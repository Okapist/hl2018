package com.pk.dao;

import com.pk.model.AllLists;
import com.pk.model.PostLists;

import static com.pk.model.PostLists.accIdAdded;

public class IndexCalculator {
    public void calculateIndexes() {

    }


    public void clearTempData() {

        PostLists.fnames.clear();
        //PostLists.fnames = null;

        PostLists.snames.clear();
        //PostLists.snames = null;

        accIdAdded.clear();
        //accIdAdded = null;

        PostLists.freeEmailDomain.clear();
        //PostLists.freeEmailDomain = null;

        PostLists.usedEmailDomain.clear();
        //PostLists.usedEmailDomain = null;


        PostLists.newEmailDomains.clear();
        //PostLists.newEmailDomains = null;

        PostLists.newEmails.clear();
        //PostLists.newEmails = null;
    }
}
