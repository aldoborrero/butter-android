/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.media.detail.movie;

import android.content.res.Resources;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.SortUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.ui.media.detail.MediaDetailPresenter;

public class MovieDetailPresenterImpl implements MovieDetailPresenter {

    private final MovieDetailView view;
    private final MediaDetailPresenter parentPresenter;
    private final YouTubeManager youTubeManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final Resources resources;

    private Movie movie;
    private String[] subtitleLanguages;

    public MovieDetailPresenterImpl(MovieDetailView view, MediaDetailPresenter parentPresenter,
            YouTubeManager youTubeManager, PreferencesHandler preferencesHandler, ProviderManager providerManager,
            PlayerManager playerManager, Resources resources) {
        this.view = view;
        this.parentPresenter = parentPresenter;
        this.youTubeManager = youTubeManager;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.resources = resources;
    }

    @Override public void onCreate(Movie movie) {
        this.movie = movie;

        if (movie != null) {
            view.initLayout(movie);
            displayMetaData();
            displayRating();
            displaySynopsis();
            displaySubtitles();
            displayQualities();
        } else {
            throw new IllegalStateException("Movie can not be null");
        }
    }

    @Override public void openTrailer() {
        if (!youTubeManager.isYouTubeUrl(movie.trailer)) {
            parentPresenter.openVideoPlayer(new StreamInfo(movie, null, null, null, null, movie.trailer));
        } else {
            parentPresenter.openYouTube(movie.trailer);
        }
    }

    @Override public void selectQuality(String quality) {
        parentPresenter.selectQuality(quality);
        view.renderHealth(movie, quality);
        view.updateMagnet(movie, quality);
    }

    @Override public void openReadMore() {
        view.showReadMoreDialog(movie.synopsis);
    }

    @Override public void playMediaClicked() {
        parentPresenter.playMediaClicked();
    }

    @Override public void subtitleSelected(int position) {
        String[] languages = this.subtitleLanguages;
        if (languages != null && languages.length > position) {
            String language = languages[position];
            parentPresenter.selectSubtitle(language);
            if (!language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
                final Locale locale = LocaleUtils.toLocale(language);
                view.setSubtitleText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
            } else {
                view.setSubtitleText(R.string.no_subs);
            }
        }
    }

    @Override public void healthClicked() {
        parentPresenter.healthClicked();
    }

    private void displayMetaData() {
        StringBuilder sb = new StringBuilder(String.valueOf(movie.year));
        if (!TextUtils.isEmpty(movie.runtime)) {
            sb.append(" • ")
                    .append(movie.runtime)
                    .append(' ')
                    .append(resources.getString(R.string.minutes));
        }

        if (!TextUtils.isEmpty(movie.genre)) {
            sb.append(" • ")
                    .append(movie.genre);
        }

        view.displayMetaData(sb);
    }

    private void displayRating() {
        if (!"-1".equals(movie.rating)) {
            Double rating = Double.parseDouble(movie.rating);
            view.displayRating(rating.intValue());
        } else {
            view.hideRating();
        }
    }

    private void displaySynopsis() {
        if (!TextUtils.isEmpty(movie.synopsis)) {
            view.displaySynopsis(movie.synopsis);
        } else {
            view.hideSynopsis();
        }
    }


    private void displaySubtitles() {
        if (providerManager.hasCurrentSubsProvider()) {
            view.setSubtitleText(R.string.loading_subs);
            view.setSubtitleEnabled(false);

            providerManager.getCurrentSubsProvider().getList(movie, new SubsProvider.Callback() {
                @Override
                public void onSuccess(Map<String, String> subtitles) {
                    if (subtitles == null || subtitles.isEmpty()) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.setSubtitleText(R.string.no_subs_available);
                            }
                        });
                        return;
                    }

                    movie.subtitles = subtitles;

                    String[] languages = subtitles.keySet().toArray(new String[subtitles.size()]);
                    Arrays.sort(languages);
                    final String[] adapterLanguages = new String[languages.length + 1];
                    adapterLanguages[0] = SubsProvider.SUBTITLE_LANGUAGE_NONE;
                    System.arraycopy(languages, 0, adapterLanguages, 1, languages.length);
                    subtitleLanguages = adapterLanguages;

                    final String[] readableNames = new String[adapterLanguages.length];
                    for (int i = 0; i < readableNames.length; i++) {
                        String language = adapterLanguages[i];
                        if (language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
                            readableNames[i] = resources.getString(R.string.no_subs);
                        } else {
                            Locale locale = LocaleUtils.toLocale(language);
                            readableNames[i] = locale.getDisplayName(locale);
                        }
                    }

                    String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                    final int defaultIndex;
                    if (subtitles.containsKey(defaultSubtitle)) {
                        defaultIndex = Arrays.asList(adapterLanguages).indexOf(defaultSubtitle);
                    } else {
                        defaultIndex = Arrays.asList(adapterLanguages).indexOf(SubsProvider.SUBTITLE_LANGUAGE_NONE);
                    }

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setSubtitleEnabled(true);
                            view.setSubsData(readableNames, defaultIndex);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            view.setSubtitleText(R.string.no_subs_available);
                            view.setSubtitleEnabled(false);
                        }
                    });
                }
            });
        } else {
            view.setSubtitleText(R.string.no_subs_available);
            view.setSubtitleEnabled(false);
        }
    }

    private void displayQualities() {

        if (movie.torrents.size() > 0) {
            final String[] qualities = movie.torrents.keySet().toArray(new String[movie.torrents.size()]);
            SortUtils.sortQualities(qualities);

            String quality = playerManager.getDefaultQuality(Arrays.asList(qualities));

            view.setQualities(qualities, quality);
            selectQuality(quality);
        }

    }

}
