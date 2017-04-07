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

package butter.droid.base.data.internal;

import android.content.Context;
import butter.droid.base.Internal;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class InternalDataModule {

    @Provides @Internal public OkHttp3Downloader provideOkHttpDownloader(OkHttpClient client) {
        return new OkHttp3Downloader(client);
    }

    @Provides @Internal public Picasso providePicasso(Context context, OkHttp3Downloader okHttpDownloader) {
        return new Picasso.Builder(context)
                .downloader(okHttpDownloader)
                .build();
    }

}
