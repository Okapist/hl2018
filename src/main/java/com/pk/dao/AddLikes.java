package com.pk.dao;

import com.pk.jsonmodel.PostLike;
import com.pk.jsonmodel.PostLikes;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

public class AddLikes {


    public HttpResponseStatus addLikes(PostLikes data) {

        boolean res = validate(data);

        if(res)
            return HttpResponseStatus.ACCEPTED;
        else
            return HttpResponseStatus.BAD_REQUEST;
    }

    private boolean validate(PostLikes data) {

        if(data.likes == null)
            return false;

        for(PostLike postLike : data.likes) {

            if(postLike == null)
                return false;

            if(postLike.likee <0 || postLike.liker<0 || postLike.likee >= AllLists.allAccounts.length ||
                    postLike.likee >= AllLists.allAccounts.length || postLike.liker >= AllLists.allAccounts.length ||
                    AllLists.allAccounts[postLike.likee] == null || AllLists.allAccounts[postLike.liker] == null)
            return false;
        }
        return true;
    }
}
