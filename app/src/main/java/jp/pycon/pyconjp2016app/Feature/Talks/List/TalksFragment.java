package jp.pycon.pyconjp2016app.Feature.Talks.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmList;
import jp.pycon.pyconjp2016app.API.Client.APIClient;
import jp.pycon.pyconjp2016app.App;
import jp.pycon.pyconjp2016app.Model.PyConJP.PresentationListEntity;
import jp.pycon.pyconjp2016app.Model.Realm.RealmDaysObject;
import jp.pycon.pyconjp2016app.Model.Realm.RealmStringObject;
import jp.pycon.pyconjp2016app.R;
import jp.pycon.pyconjp2016app.Util.RealmUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by rhoboro on 4/22/16.
 */
public class TalksFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private Context mContext;
    private View view;
    private Realm realm;

    public static TalksFragment newInstance() {
        return new TalksFragment();
    }

    public TalksFragment() {
        // nop
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        realm = Realm.getDefaultInstance();
        final RealmDaysObject days = realm.where(RealmDaysObject.class).findFirst();
//        if (days != null) {
//            setupTalkList();
//        } else {
            getPyConJPSchedule();
//        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ViewPager pager = (ViewPager)view.findViewById(R.id.view_pager);
        pager.setAdapter(null);
        realm.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void getPyConJPSchedule() {
        APIClient apiClient = ((App)getActivity().getApplication()).getAPIClient();
        rx.Observable<PresentationListEntity> observable = apiClient.getPyConJPTalks();
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PresentationListEntity>() {
                               @Override
                               public void onCompleted() {
                                   setupTalkList();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   e.printStackTrace();
                                   Toast.makeText(mContext, "error" + e, Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onNext(PresentationListEntity presentationList) {
                                   RealmUtil.savePresentationList(realm, presentationList);
                               }
                           }
                );
    }

    private void setupTalkList() {
        TabLayout tab= (TabLayout)view.findViewById(R.id.tab_layout);
        tab.setTabMode(TabLayout.MODE_FIXED);
        tab.setTabGravity(TabLayout.GRAVITY_FILL);
        ViewPager pager = (ViewPager)view.findViewById(R.id.view_pager);

        final RealmDaysObject days = realm.where(RealmDaysObject.class).findFirst();
        final RealmList<RealmStringObject> list = days.getDays();

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                TalkListFragment f = TalkListFragment.newInstance(position, false);
                return f;
            }

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position).getString();
            }
        };
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(this);
        tab.setupWithViewPager(pager);
    }
}
