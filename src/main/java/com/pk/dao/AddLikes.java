package com.pk.dao;

import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

public class AddLikes {

    public HttpResponseStatus addLikes(List<int[]> data) {

        boolean res = addLikesHelper(data);

        if(res)
            return HttpResponseStatus.ACCEPTED;
        else
            return HttpResponseStatus.BAD_REQUEST;
    }

    private boolean addLikesHelper(List<int[]> likes) {

        if (likes != null) {

            for (int i = 0; i < likes.size(); ++i) {
                int[] like = likes.get(i);
                int likeFromId = like[0];
                int likeToId = like[1];

                if(likeFromId >= AllLists.allAccounts.length || AllLists.allAccounts[likeFromId] == null)
                    return false;
                if(likeToId >= AllLists.allAccounts.length || AllLists.allAccounts[likeToId] == null)
                    return false;
                if(like[2] <= 0)
                    return false;
            }
            return true;
        }
        return false;
    }
}
