package com.emanuelef.remote_capture.activities;

import java.util.Comparator;
import java.text.RuleBasedCollator;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;
import java.io.File;

public final class filecomparator implements Comparator<String> {
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_BY_TIME = 3;
    public static final int SORT_BY_TYPE = 0;
    private static final String TAG = "StringComparator";
    private static filecomparator sInstance = new filecomparator();
    private RuleBasedCollator mCollator = null;
    private int mSortType = 0;
    private String pat="";

    private filecomparator() {
    }

    private void setSortType(int sort,String pat) {
        this.mSortType = sort;
        this.pat=pat;
        if (this.mCollator == null) {
            this.mCollator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);
        }
    }

    public static filecomparator getInstance(int sort,String pat) {
        sInstance.setSortType(sort,pat);
        return sInstance;
    }

    public int compare(String op, String oq) {
        boolean isOpDirectory = new File(pat+op).isDirectory();
        if ((isOpDirectory ^ new File(pat+ oq).isDirectory()) != false) {
            //LogUtils.v(TAG, op.getFileName() + " vs " + oq.getFileName() + " result=" + (isOpDirectory ? -1 : 1));
            if (isOpDirectory) {
                return -1;
            }
            return 1;
        }
        switch (this.mSortType) {
            case SORT_BY_TYPE /*0*/:
                return sortByType(op, oq);
            case SORT_BY_NAME /*1*/:
                return sortByName(op, oq);
            case SORT_BY_SIZE /*2*/:
                return sortBySize(op, oq);
            case SORT_BY_TIME /*3*/:
                return sortByTime(op, oq);
            default:
                return sortByName(op, oq);
        }
    }

    private int sortByType(String op, String oq) {
        boolean isOpDirectory = new File(pat+op).isDirectory();
        boolean isOqDirectory = new File(pat+oq).isDirectory();
        if (isOpDirectory && isOqDirectory) {
            boolean isOpCategoryFolder = false;
            if ((isOpCategoryFolder ^ false) != false) {
                int i;
                String str = TAG;
                StringBuilder append = new StringBuilder().append(getFileName(op)).append(" - ").append(getFileName(oq)).append(" result=");
                if (isOpCategoryFolder) {
                    i = -1;
                } else {
                    i = 1;
                }
                // LogUtils.i(str, append.append(i).toString());
                if (isOpCategoryFolder) {
                    return -1;
                }
                return 1;
            }
        }
        if (!(isOpDirectory || isOqDirectory)) {
            String opExtension = getFileExtension(getFileName(op));
            String oqExtension = getFileExtension(getFileName(oq));
            if (opExtension == null && oqExtension != null) {
                return -1;
            }
            if (opExtension != null && oqExtension == null) {
                return 1;
            }
            if (!(opExtension == null || oqExtension == null || opExtension.equalsIgnoreCase(oqExtension))) {
                return opExtension.compareToIgnoreCase(oqExtension);
            }
        }
        return sortByName(op, oq);
    }

    private int sortByName(String op, String oq) {
        return this.mCollator.compare(this.mCollator.getCollationKey(getFileName(op)).getSourceString(), this.mCollator.getCollationKey(getFileName(oq)).getSourceString());
    }

    private int sortBySize(String op, String oq) {
        if (!(new File(pat+op).isDirectory() || new File(pat+oq).isDirectory())) {
            long opSize = getFileSize(op);
            long oqSize = getFileSize(oq);
            if (opSize != oqSize) {
                return opSize > oqSize ? -1 : 1;
            }
        }
        return sortByName(op, oq);
    }

    private int sortByTime(String op, String oq) {
        long opTime = getFileLastModifiedTime(op);
        long oqTime = getFileLastModifiedTime(oq);
        if (opTime != oqTime) {
            return opTime > oqTime ? -1 : 1;
        } else {
            return sortByName(op, oq);
        }
    }
    private String getFileName(String name){
        return name;
    }
    private long getFileSize(String name){
        return new File(pat+name).length();
    }
    private long getFileLastModifiedTime(String name){
        return new File(pat+name).lastModified();
    }
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDot = fileName.lastIndexOf(46);
        if (lastDot >= 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return null;
    }
}
