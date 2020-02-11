package net.ivpn.client.ui.tutorial.data;

public class TutorialPageContent {
    private int imgResId;
    private int titleResId;
    private int descrResId;
    private boolean isEmptyTitle;

    public TutorialPageContent(int imgResId, int titleResId, int descrResId, boolean isEmptyTitle) {
        this.imgResId = imgResId;
        this.titleResId = titleResId;
        this.descrResId = descrResId;
        this.isEmptyTitle = isEmptyTitle;
    }

    public int getImgResId() {
        return imgResId;
    }

    public void setImgResId(int imgResId) {
        this.imgResId = imgResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public void setTitleResId(int titleResId) {
        this.titleResId = titleResId;
    }

    public int getDescrResId() {
        return descrResId;
    }

    public void setDescrResId(int descrResId) {
        this.descrResId = descrResId;
    }

    public boolean isEmptyTitle() {
        return isEmptyTitle;
    }

    public void setEmptyTitle(boolean emptyTitle) {
        isEmptyTitle = emptyTitle;
    }
}
