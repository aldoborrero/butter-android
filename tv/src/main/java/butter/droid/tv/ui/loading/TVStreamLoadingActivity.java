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

package butter.droid.tv.ui.loading;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import butter.droid.base.providers.media.models.Show;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.tv.activities.base.TVBaseActivity;

public class TVStreamLoadingActivity extends TVBaseActivity implements TVStreamLoadingView {

	private final static String EXTRA_STREAM_INFO = "butter.droid.ui.loading.StreamLoadingActivity.info";
	public final static String EXTRA_SHOW_INFO = "butter.droid.ui.loading.StreamLoadingActivity.show_info";

//	@Inject TVStreamLoadingPresenter presenter;
//
//	@Nullable private BaseStreamLoadingFragment fragment;
//
//	@SuppressLint("MissingSuperCall")
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		TVButterApplication.getAppContext()
//				.getComponent()
//				.streamLoadingComponentBuilder()
//				.streamLoadingModule(new TVStreamLoadingModule(this))
//				.build()
//				.inject(this);
//
//		super.onCreate(savedInstanceState, 0);
//
//		StreamInfo streamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
//		presenter.onCreate(streamInfo, savedInstanceState != null);
//	}
//
//	@Override
//	public void onBackPressed() {
//		super.onBackPressed();
//		fragment.cancelStream();
//	}
//
//	@Override
//    public void onTorrentServiceDisconnected() {
//		if (null != fragment) {
//			fragment.onTorrentServiceDisconnected();
//		}
//	}
//
//	@Override
//	public void onTorrentServiceConnected() {
//		if (null != fragment) {
//			fragment.onTorrentServiceConnected();
//		}
//	}
//
	@Override public void displayStreamLoadingFragment(@NonNull StreamInfo info) {
//		TVStreamLoadingFragment fragment = TVStreamLoadingFragment.newInstance(info);
//		getSupportFragmentManager()
//				.beginTransaction()
//				.add(android.R.id.content, fragment)
//				.commit();
//
//		this.fragment = fragment;
//
	}

	public static Intent startActivity(Activity activity, StreamInfo info) {
		Intent i = new Intent(activity, TVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);
		activity.startActivity(i);
		return i;
	}

	public static Intent startActivity(Activity activity, StreamInfo info, Show show) {
		Intent i = new Intent(activity, TVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);
		i.putExtra(EXTRA_SHOW_INFO, show);
		activity.startActivity(i);
		return i;
	}

	public static Intent startActivity(Activity activity, StreamInfo info, Pair<View, String>... elements) {
		Intent i = new Intent(activity, TVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);

		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				activity, elements);
		ActivityCompat.startActivity(activity, i, options.toBundle());
		return i;
	}

}
