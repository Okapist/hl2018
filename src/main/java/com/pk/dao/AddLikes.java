package com.pk.dao;

import com.pk.jsonmodel.PostLikes;
import com.pk.model.AllLists;
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

        if(data.likeData == null)
            return false;

        for(int[] postLike : data.likeData) {

            if(postLike == null)
                return false;

            if(postLike[1] <=0 || postLike[0]<=0 || postLike[0] >= AllLists.allAccounts.length ||
                    postLike[1] >= AllLists.allAccounts.length ||
                    AllLists.allAccounts[postLike[1]] == null || AllLists.allAccounts[postLike[0]] == null)
            return false;
        }
        return true;
    }
}
