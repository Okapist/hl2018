package com.pk.webserver;

import com.pk.dao.*;
import com.pk.model.AllLists;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Workers {

    public HttpResponseStatus filter(HttpRequest request, StringBuilder buf) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();

        Boolean sex = null;

        String email = null;
        Boolean emailDomain = null;
        Boolean emailLt = null;

        String status = null;
        Boolean statusEq = null;

        List<String> fname = null;
        Boolean fnameExists = null;

        String sname = null;
        Boolean snameEq = null;
        Boolean snameExists = null;

        String phoneCode = null;
        Boolean phoneExists = null;

        String country = null;
        Boolean countryExists = null;

        List<String> city = null;
        Boolean cityExists = null;

        Integer birth = null;
        Boolean birthLt = null;
        Boolean birthYear = null;

        List<String> interests = null;
        Boolean interestsAll = null;

        List<String> likes = null; //int real

        Boolean premiumNow = null;
        Boolean premiumAny = null;

        int limit = 0;
        try {
            for (String key : params.keySet()) {
                switch (key) {
                    case "sex_eq":
                        sex = "m".equals(params.get(key).get(0));
                        break;
                    case "email_domain":
                        email = params.get(key).get(0).substring(params.get(key).get(0).indexOf('@') + 1);
                        emailDomain = true;
                        break;
                    case "email_lt":
                        email = params.get(key).get(0);
                        emailLt = true;
                        break;
                    case "email_gt":
                        email = params.get(key).get(0);
                        emailLt = false;
                        break;
                    case "status_eq":
                        status = params.get(key).get(0);
                        statusEq = true;
                        break;
                    case "status_neq":
                        status = params.get(key).get(0);
                        statusEq = false;
                        break;
                    case "fname_eq":
                        fname = new ArrayList<>();
                        fname.add(params.get(key).get(0));
                    case "fname_any":
                        fname = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        break;
                    case "fname_null":
                        fname = new ArrayList<>();
                        fnameExists = Integer.parseInt(params.get(key).get(0)) == 0;
                        break;
                    case "sname_eq":
                        sname = params.get(key).get(0);
                        snameEq = true;
                        break;
                    case "sname_starts":
                        sname = params.get(key).get(0);
                        snameEq = false;
                        break;
                    case "sname_null":
                        sname = "";
                        snameExists = Integer.parseInt(params.get(key).get(0)) == 0;
                        break;
                    case "phone_code":
                        phoneCode = params.get(key).get(0);
                        break;
                    case "phone_null":
                        phoneCode = "";
                        phoneExists = Integer.parseInt(params.get(key).get(0)) == 0;
                        break;
                    case "country_eq":
                        country = params.get(key).get(0);
                        break;
                    case "country_null":
                        country = "";
                        countryExists = Integer.parseInt(params.get(key).get(0)) == 0;
                        break;
                    case "city_eq":
                        city = new ArrayList<>();
                        city.add(params.get(key).get(0));
                    case "city_any":
                        city = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        break;
                    case "city_null":
                        city = new ArrayList<>();
                        cityExists = Integer.parseInt(params.get(key).get(0)) == 0;
                        break;
                    case "birth_lt":
                        birth = Integer.parseInt(params.get(key).get(0));
                        birthLt = true;
                        break;
                    case "birth_gt":
                        birth = Integer.parseInt(params.get(key).get(0));
                        birthLt = false;
                        break;
                    case "birth_year":
                        birth = Integer.parseInt(params.get(key).get(0));
                        birthYear = true;
                        break;
                    case "interests_contains":
                        interests = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        interestsAll = true;
                        break;
                    case "interests_any":
                        interests = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        interestsAll = false;
                        break;
                    case "likes_contains":
                        likes = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        break;
                    case "premium_now":
                        premiumNow = Integer.parseInt(params.get(key).get(0)) == 1;
                        break;
                    case "premium_null":
                        premiumAny = Integer.parseInt(params.get(key).get(0)) == 1;
                        break;
                    case "limit":
                        limit = Integer.parseInt(params.get(key).get(0));
                        if (limit < 0)
                            return HttpResponseStatus.BAD_REQUEST;
                        break;
                    case "query_id":
                        break;
                    default:
                        return HttpResponseStatus.BAD_REQUEST;
                }
            }
        } catch (Exception ex) {
            return HttpResponseStatus.BAD_REQUEST;
        }

        Byte statusToFiler = null;
        if (status != null) {
            switch (status) {
                case "свободны":
                    statusToFiler = 1;
                    break;
                case "всё сложно":
                    statusToFiler = 2;
                    break;
                case "заняты":
                    statusToFiler = 3;
                    break;
            }
        }

        List<Short> cityIndex = convertCityListToIndex(city);
        Short countryIndex = convertCountryToIndex(country);

        if((city != null && cityIndex.size()==0 && cityExists== null) ||
                (country != null && countryIndex == null && countryExists == null)) {
            String fullResponse = "{\"accounts\": []}";
            buf.append(fullResponse);
            //request.response(fullResponse, HttpResponseStatus.OK).end();
            return HttpResponseStatus.OK;
        }

        buf.append("{\"accounts\": [");
        NewAccFilter accFilter = new NewAccFilter();

        char[][] fnameCharArr = null;
        if(fname != null && fname.size() > 0) {
            fnameCharArr = new char[fname.size()][];
            for (int i = 0; i < fname.size(); i++) {
                String p = fname.get(i);
                if (p != null) fnameCharArr[i] = p.toCharArray();
            }
        }

        boolean result = accFilter.filter(sex,
                email==null?null:email.toCharArray(), emailDomain, emailLt,
                statusToFiler, statusEq,
                fnameCharArr, fnameExists,
                sname==null?null:sname.toCharArray(), snameExists, snameEq,
                phoneCode, phoneExists,
                countryIndex, countryExists,
                cityIndex, cityExists,
                birth, birthLt, birthYear,
                interests, interestsAll,
                likes,
                premiumNow, premiumAny,
                limit,
                buf);

        buf.append("]}");
        return HttpResponseStatus.OK;
    }

    public HttpResponseStatus group(HttpRequest request, StringBuilder buf) {

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();

        Boolean sex = null;
        String status = null;
        String fname = null;
        String sname = null;
        String country = null;
        String city = null;
        Integer birth = null;
        String interests = null;
        Integer joined = null;

        String likes = null; //int real
        boolean order = false;

        int limit = 0;
        List<String> group = null;
        try {
            for (String key : params.keySet()) {
                switch (key) {
                    case "keys":
                        group = new ArrayList<>(Arrays.asList(params.get(key).get(0).split(",")));
                        for (String g : group) {
                            if (!"sex".equals(g) && !"status".equals(g) && !"interests".equals(g) && !"country".equals(g) && !"city".equals(g)) {
                                return HttpResponseStatus.BAD_REQUEST;
                            }
                        }
                        if(group == null || group.size() == 0) {
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "sex":
                        sex = "m".equals(params.get(key).get(0));
                        break;
                    case "status":
                        status = params.get(key).get(0);
                        break;
                    case "interests":
                        interests = params.get(key).get(0);
                        break;
                    case "country":
                        country = params.get(key).get(0);
                        break;
                    case "city":
                        city = params.get(key).get(0);
                        break;
                    case "fname":
                        fname = params.get(key).get(0);
                        break;
                    case "sname":
                        sname = params.get(key).get(0);
                        break;
                    case "birth":
                        birth = Integer.parseInt(params.get(key).get(0));
                        break;
                    case "likes":
                        likes = params.get(key).get(0);
                        break;
                    case "joined":
                        joined = Integer.parseInt(params.get(key).get(0));
                        break;
                    case "limit":
                        limit = Integer.parseInt(params.get(key).get(0));
                        if (limit < 0)
                            return HttpResponseStatus.BAD_REQUEST;
                        break;
                    case "order":
                        order = "1".equals(params.get(key).get(0));
                        break;
                    case "query_id":
                        break;
                    default:
                        return HttpResponseStatus.BAD_REQUEST;
                }
            }
        } catch (Exception ex) {
            return HttpResponseStatus.BAD_REQUEST;
        }

        NewAccGroup accGroup = new NewAccGroup();

        Byte statusToFiler = null;
        if (status != null) {
            switch (status) {
                case "свободны":
                    statusToFiler = 1;
                    break;
                case "всё сложно":
                    statusToFiler = 2;
                    break;
                case "заняты":
                    statusToFiler = 3;
                    break;
            }
        }

        Short cityIndex = convertCityToIndex(city);
        Short countryIndex = convertCountryToIndex(country);

        if((cityIndex == null && city != null) ||
                (country != null && countryIndex == null) ||
                group == null) {
            String fullResponse = "{\"accounts\": []}";
            buf.append(fullResponse);
            return HttpResponseStatus.OK;
        }

        buf.append("{\"groups\": [");
        boolean result = accGroup.getGroups(group, sex, statusToFiler, interests, countryIndex, cityIndex,
                birth, likes, joined, limit, order, buf);
        if (result) {
            buf.append("]}");
            return HttpResponseStatus.OK;
        }
        else {
            buf.append("{}");
            return HttpResponseStatus.NOT_FOUND;
       }
    }

    public HttpResponseStatus recommend(HttpRequest request, StringBuilder buf){

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();

        int accId;
        try{
            accId = Integer.parseInt(request.uri().split("/")[2]);
        } catch (Exception ex) {
            buf.append("{}");
            return HttpResponseStatus.BAD_REQUEST;
        }

        String country = null;
        String city = null;
        int limit = 0;
        try {
            for (String key : params.keySet()) {
                switch (key) {
                    case "country":
                        country = params.get(key).get(0);
                        if("".equals(country)) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "city":
                        city = params.get(key).get(0);
                        if("".equals(city)) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "limit":
                        limit = Integer.parseInt(params.get(key).get(0));
                        if(limit<0) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "query_id":
                        break;
                    default:
                        buf.append("{}");
                        return HttpResponseStatus.BAD_REQUEST;
                }
            }
        } catch (Exception ex) {
            buf.append("{}");
            return HttpResponseStatus.BAD_REQUEST;
        }

        if(accId > AllLists.allAccounts.length || AllLists.allAccounts[accId] == null) {
            buf.append("{}");
            return HttpResponseStatus.NOT_FOUND;
        }

        Short cityIndex = convertCityToIndex(city);
        Short countryIndex = convertCountryToIndex(country);

        if((cityIndex == null && city != null) ||
                (country != null && countryIndex == null)) {
            String fullResponse = "{\"accounts\": []}";
            buf.append(fullResponse);
            return HttpResponseStatus.OK;
        }

        NewRecommend recommend = new NewRecommend();
        buf.append("{\"accounts\": [");
        boolean result = recommend.recommend(accId, countryIndex, cityIndex, limit, buf);
        if(result) {
            buf.append("]}");
            return HttpResponseStatus.OK;
        }
        else {
            buf.append("{}");
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    public HttpResponseStatus suggest(HttpRequest request, StringBuilder buf) {

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        int accId;
        try{
            accId = Integer.parseInt(request.uri().toString().split("/")[2]);
        } catch (Exception ex) {
            buf.append("{}");
            return HttpResponseStatus.BAD_REQUEST;
        }

        String country = null;
        String city = null;
        int limit = 0;
        try {
            for (String key : params.keySet()) {
                switch (key) {
                    case "country":
                        country = params.get(key).get(0);
                        if("".equals(country)) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "city":
                        city = params.get(key).get(0);
                        if("".equals(city)) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "limit":
                        limit = Integer.parseInt(params.get(key).get(0));
                        if(limit<0) {
                            buf.append("{}");
                            return HttpResponseStatus.BAD_REQUEST;
                        }
                        break;
                    case "query_id":
                        break;
                    default:
                        buf.append("{}");
                        return HttpResponseStatus.BAD_REQUEST;
                }
            }
        } catch (Exception ex) {
            buf.append("{}");
            return HttpResponseStatus.BAD_REQUEST;
        }

        NewSuggest suggest = new NewSuggest();
        if(accId > AllLists.allAccounts.length || AllLists.allAccounts[accId] == null) {
            buf.append("{}");
            return HttpResponseStatus.NOT_FOUND;
        }

        Short cityIndex = convertCityToIndex(city);
        Short countryIndex = convertCountryToIndex(country);

        if((cityIndex == null && city != null) ||
                (country != null && countryIndex == null)) {
            String fullResponse = "{\"accounts\": []}";
            buf.append(fullResponse);
            return HttpResponseStatus.OK;
        }

        buf.append("{\"accounts\": [");
        boolean result = suggest.suggest(accId, countryIndex, cityIndex, limit, buf);
        if(result) {
            buf.append("]}");
            return HttpResponseStatus.OK;
        }else {
            buf.append("]}");
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    public HttpResponseStatus newAccount (HttpRequest request, StringBuilder buf) {
        buf.append("{}");
        return HttpResponseStatus.CREATED;
    }

    public HttpResponseStatus likes(HttpRequest request, StringBuilder buf) {
        buf.append("{}");
        return HttpResponseStatus.ACCEPTED;
    }

    public HttpResponseStatus refresh(HttpRequest request, StringBuilder buf) {
        buf.append("{}");
        return HttpResponseStatus.ACCEPTED;
    }

    private List<Short> convertCityListToIndex(List<String> city) {
        if(city == null)
            return null;
        List<Short> cityIndexes = new ArrayList<>(city.size());
        for(String c: city) {
            Short i = Utils.findCityIndex(c);
            if(i!= null)
                cityIndexes.add(i);
        }
        return cityIndexes;
    }

    private Short convertCountryToIndex(String country) {
        if(country == null)
            return null;
        return Utils.findCountryIndex(country);
    }

    private Short convertCityToIndex(String city) {
        if(city == null)
            return null;
        return Utils.findCityIndex(city);
    }
}
