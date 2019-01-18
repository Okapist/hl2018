package com.pk.dao;

import com.pk.model.AllLists;
import com.pk.model.PostLists;

import static com.pk.model.PostLists.accIdAdded;
import static com.pk.model.PostLists.accIdEdited;

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

        accIdEdited.clear();
        accIdEdited = null;

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
