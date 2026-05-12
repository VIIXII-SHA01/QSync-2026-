package android.bignerdranch.com.qsync;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewKioskPagerAdapter extends FragmentStateAdapter {

    public ViewKioskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new KioskClientsFragment();
            case 2:
                return new KioskInfoFragment();
            default:
                return new KioskHostFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
