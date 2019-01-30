package com.pk.dao;

import com.pk.Runner;
import com.pk.model.AllLists;
import com.pk.model.PostLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

import static com.pk.model.AllLists.*;

public class AddLikes {

    public HttpResponseStatus addLikes(List<int[]> data) {

        if(!Runner.isWarm) {
            boolean res = addLikesHelper(data);

            if (res)
                return HttpResponseStatus.ACCEPTED;
            else
                return HttpResponseStatus.BAD_REQUEST;
        } else {
            return HttpResponseStatus.ACCEPTED;
        }
    }

    private boolean addLikesHelper(List<int[]> allLikes) {

        if (allLikes != null) {
            for (int i = 0; i < allLikes.size(); ++i) {
                int[] like = allLikes.get(i);
                int likeFromId = like[0];
                int likeToId = like[1];
                int likeTs = like[2];

                if (likeFromId >= AllLists.allAccounts.length || AllLists.allAccounts[likeFromId] == null)
                    return false;

                if (likeToId >= AllLists.allAccounts.length || AllLists.allAccounts[likeToId] == null)
                    return false;

                if (likeTs <= 0)
                    return false;
            }
        }

        if (allLikes != null) {
            for (int i = 0; i < allLikes.size(); ++i) {

                int[] like = allLikes.get(i);
                int likeFromId = like[0];
                int likeToId = like[1];
                int likeTs = like[2];

                while (likesAccounts.size() <= likeFromId)
                    likesAccounts.add(null);

                int[] accLikes = likesAccounts.get(likeFromId);
                int startPos = lastlikesAccountsPointers[likeFromId] + 1;

                if(accLikes == null) {
                    likesAccounts.set(likeFromId, new int[4]);
                    startPos = 0;
                } else {
                    if (accLikes.length - startPos < 3) {
                        int newReqSize = 1 * 2 - (accLikes.length - startPos) + accLikes.length;
                        int[] newAccLikes = new int[Math.max((int) (newReqSize * 1.1), newReqSize + 2)];
                        System.arraycopy(accLikes, 0, newAccLikes, 0, accLikes.length);
                        likesAccounts.set(likeFromId, newAccLikes);
                    }
                }

                likesAccounts.get(likeFromId)[startPos] = likeToId;
                ++startPos;
                likesAccounts.get(likeFromId)[startPos] = likeTs;
                ++startPos;

                PostLists.likesFromSort.add(likeFromId);

                if (likeToId >= likesTO.length)
                    return false;
                int[] lTo = likesTO[likeToId];

                int startToPos = lastLikeToPointers[likeToId] + 1;
                if(lTo == null) {
                    startToPos = 0;
                    likesTO[likeToId] = new int[4];
                } else {
                    if (lTo.length - startToPos < 1) {
                        int newReqSize = 1 - (lTo.length - startToPos) + lTo.length;
                        int[] newlTo = new int[Math.max((int) (newReqSize * 1.1), newReqSize + 2)];
                        System.arraycopy(lTo, 0, newlTo, 0, lTo.length);
                        likesTO[likeToId] = newlTo;
                    }
                }

                likesTO[likeToId][startToPos] = likeFromId;
                PostLists.likesToSort.add(likeToId);
                ++lastLikeToPointers[likeToId];
                lastlikesAccountsPointers[likeFromId] += 2;
            }

            return true;
        }
        return true;
    }
}
