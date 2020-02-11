package net.ivpn.client.ui.tutorial;

import net.ivpn.client.R;
import net.ivpn.client.ui.tutorial.data.TutorialPageContent;

public enum TutorialPages {
    SECURITY(new TutorialPageContent(R.drawable.tutorial_page_security, R.string.tutorial_page_title_security,
            R.string.tutorial_page_descr_security, true)),
    ACCESS(new TutorialPageContent(R.drawable.tutorial_page_access, R.string.tutorial_page_title_access,
            R.string.tutorial_page_descr_access, false)),
    LOCATIONS(new TutorialPageContent(R.drawable.tutorial_page_locations, R.string.tutorial_page_title_locations,
            R.string.tutorial_page_descr_locations, false));
//    MULTIHOP(new TutorialPageContent(R.drawable.tutorial_page_multihop, R.string.tutorial_page_title_multihop,
//            R.string.tutorial_page_descr_multihop, false));

    public static final String PAGE_POSITION = "TUTORIAL_PAGE_POSITION";

    private TutorialPageContent tutorialPageContent;

    TutorialPages(TutorialPageContent tutorialPageContent) {
        this.tutorialPageContent = tutorialPageContent;
    }

    public TutorialPageContent getTutorialPageContent() {
        return tutorialPageContent;
    }
}
