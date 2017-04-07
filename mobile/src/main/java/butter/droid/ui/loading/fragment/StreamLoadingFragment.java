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

package butter.droid.ui.loading.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.base.utils.VersionUtils;
import butter.droid.ui.loading.StreamLoadingActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StreamLoadingFragment extends BaseStreamLoadingFragment implements StreamLoadingFragmentView {

    @Inject StreamLoadingFragmentPresenter presenter;
    @Inject Picasso picasso;

    @BindView(R.id.background_imageview) ImageView backgroundImageView;
    @BindView(R.id.startexternal_button) Button startExternalButton;

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        ((StreamLoadingActivity) context).getComponent()
                .streamLoadingFragmentComponentBuilder()
                .stramLoadingFragmentModule(new StreamLoadingFragmentModule(this))
                .build()
                .inject(this);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARGS_STREAM_INFO);
        presenter.onCreate(streamInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamloading, container, false);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (VersionUtils.isLollipop()) {
            //postpone the transitions until after the view is layed out.
            getActivity().postponeEnterTransition();

            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override public void loadBackgroundImage(String url) {
        picasso.load(url).error(R.color.bg).into(backgroundImageView);
    }

    @Override public void pickTorrentFile(String[] fileNames) {
        StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.select_file, fileNames, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        presenter.selectTorrentFile(position);
                    }
                });
    }

    @Override public void startBeamActivity(StreamInfo streamInfo, int resumePosition) {
        getActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), streamInfo, resumePosition));
    }

    @Override public void closeSelf() {
        getActivity().finish();
    }

    @Override public void startExternalPlayer(@NonNull Intent intent) {
        startActivity(intent);
    }

    @Override public void startPlayerActivity(StreamInfo streamInfo, int resumePosition) {
        startActivity(VideoPlayerActivity.getIntent(getContext(), streamInfo, resumePosition));
    }

    @Override public void showExternalPlayerButton() {
        startExternalButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.startexternal_button) public void externalClick() {
        presenter.startExternalPlayer();
    }

    public static StreamLoadingFragment newInstance(@NonNull StreamInfo streamInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STREAM_INFO, streamInfo);

        StreamLoadingFragment fragment = new StreamLoadingFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
