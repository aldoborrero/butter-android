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

package butter.droid.tv.activities.base;

import android.os.Bundle;
import butter.droid.tv.ui.search.TVSearchActivity;

public abstract class TVBaseActivity extends TVTorrentBaseActivity {

//	@Inject ButterUpdateManager updateManager;

	@Override
	protected void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState, layoutId);

//		if(!VersionUtils.isUsingCorrectBuild()) {
//			new AlertDialog.Builder(this)
//					.setMessage(butter.droid.base.R.string.wrong_abi)
//					.setCancelable(false)
//					.show();
//
//			updateManager.setListener(new ButterUpdateManager.Listener() {
//				@Override
//				public void onNewUpdateAvailable(String updateFile) {
//					Intent installIntent = new Intent(Intent.ACTION_VIEW);
//					installIntent.setDataAndType(Uri.parse("file://" + updateFile), ButterUpdateManager.ANDROID_PACKAGE_MIME_TYPE);
//					installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(installIntent);
//				}
//			});
//			updateManager.checkUpdatesManually();
//		}
	}

	@Override
	public boolean onSearchRequested() {
		TVSearchActivity.startActivity(this);
		return true;
	}

}
