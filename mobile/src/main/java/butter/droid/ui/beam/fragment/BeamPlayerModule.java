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

package butter.droid.ui.beam.fragment;

import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.beam.BeamPlayerActivityPresenter;
import dagger.Module;
import dagger.Provides;

@Module
public class BeamPlayerModule {

    private final BeamPlayerView view;

    public BeamPlayerModule(final BeamPlayerView view) {
        this.view = view;
    }

    @Provides @FragmentScope BeamPlayerView provideView() {
        return view;
    }

    @Provides @FragmentScope BeamPlayerPresenter providePresenter(BeamPlayerView view, BeamManager beamManager,
            BeamPlayerActivityPresenter parentPresenter) {
        return new BeamPlayerPresenterImpl(view, beamManager, parentPresenter);
    }
}
