package com.pk.dao;

import com.pk.model.AllLists;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

import static com.pk.model.AllLists.likesAccounts;
import static com.pk.model.AllLists.likesTO;

public class AddLikes {

    public HttpResponseStatus addLikes(List<int[]> data) {

        boolean res = addLikesHelper(data);

        if(res)
            return HttpResponseStatus.ACCEPTED;
        else
            return HttpResponseStatus.BAD_REQUEST;
    }

    private boolean addLikesHelper(List<int[]> allLikes) {

        if (allLikes != null) {
            for (int i = 0; i < allLikes.size(); ++i) {
                int[] like = allLikes.get(i);
                int likeFromId = like[0];
                int likeToId = like[1];
                int likeTs =  like[2];

                if (likeToId >= AllLists.allAccounts.length || AllLists.allAccounts[likeToId] == null)
                    return false;

                if (like[1] <= 0)
                    return false;


                while (likesAccounts.size() <= likeFromId)
                    likesAccounts.add(null);

                int[] accLikes = likesAccounts.get(likeFromId);
                int startPos = Integer.MAX_VALUE;
                if (accLikes == null) {
                    likesAccounts.set(likeFromId, new int[4]);
                    accLikes = likesAccounts.get(likeFromId);
                    startPos = 0;
                } else {
                    for (int i1 = accLikes.length - 1; i1 >= 0; --i1) {
                        if (accLikes[i1] != 0) {
                            startPos = i1 + 1;
                            break;
                        }
                    }
                    if (startPos == Integer.MAX_VALUE)
                        startPos = 0;
                }
                if (accLikes.length - startPos < 3) {
                    int newReqSize = 1 * 2 - (accLikes.length - startPos) + accLikes.length;
                    int[] newAccLikes = new int[Math.max((int) (newReqSize * 1.1), newReqSize + 2)];
                    System.arraycopy(accLikes, 0, newAccLikes, 0, accLikes.length);
                    likesAccounts.set(likeFromId, newAccLikes);
                }

                likesAccounts.get(likeFromId)[startPos] = likeToId;
                ++startPos;
                likesAccounts.get(likeFromId)[startPos] = likeTs;
                ++startPos;


                if (likeToId >= likesTO.length)
                    return false;
                int[] lTo = likesTO[likeToId];

                int startToPos = Integer.MAX_VALUE;
                if (lTo == null) {
                    likesTO[likeToId] = new int[2];
                    lTo = likesTO[likeToId];
                    startToPos = 0;
                } else {
                    for (int j = lTo.length - 1; j >= 0; --j) {
                        if (lTo[j] != 0) {
                            startToPos = j + 1;
                            break;
                        }
                    }
                    if (startToPos == Integer.MAX_VALUE)
                        startToPos = 0;
                }
                if (lTo.length - startToPos < 1) {
                    int newReqSize = 1 - (lTo.length - startToPos) + lTo.length;
                    int[] newlTo = new int[Math.max((int) (newReqSize * 1.1), newReqSize + 2)];
                    System.arraycopy(lTo, 0, newlTo, 0, lTo.length);
                    likesTO[likeToId] = newlTo;
                }

                likesTO[likeToId][startToPos] = likeFromId;
            }

            return true;
        }
        return false;
    }
}
