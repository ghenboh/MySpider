package com.worm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.worm.Bean.Score;

public class SchoolScore {//对比的是同省同专业的相应数字之差及其平均数，总的最高最低之平均
    String name;
    Map<String, Map<String, Score>> majorMainMap;
    Map<String, Map<String, Score>> storageMap;
    Map<String, Set<Score>> highInProvince;
    Map<String, Set<Score>> lowInProvince;
    Set<String> outOfProvince;
    Map<String, Set<String>> majors;

    public SchoolScore(String name) {
        this.name = name;
        highInProvince = new HashMap<>();
        lowInProvince = new HashMap<>();
        storageMap = new HashMap<>();
        majorMainMap = new HashMap<>();
        outOfProvince = new HashSet<>(Set.of("内蒙古", "海南", "西藏", "青海", "宁夏"));
        majors = new HashMap<>();
    }

    public void addScore(Score score) {
        String province = score.getProvince();
        String major = score.getMajor();
        if(!storageMap.containsKey(province)) {
            storageMap.put(province, new HashMap<>());
        }
        storageMap.get(province).put(major, score);
        if(!majorMainMap.containsKey(major)) {
            majorMainMap.put(major, new HashMap<>());
        }
        majorMainMap.get(major).put(province, score);
        if(!highInProvince.containsKey(province) || highInProvince.get(province).iterator().next().getMaxScore() < score.getMaxScore()) {
            highInProvince.put(province, new HashSet<>(Set.of(score)));
        } else if(highInProvince.get(province).iterator().next().getMaxScore() == score.getMaxScore()) {
            highInProvince.get(province).add(score);
        }
        if(!lowInProvince.containsKey(province) || lowInProvince.get(province).iterator().next().getMaxScore() > score.getMaxScore()) {
            lowInProvince.put(province, new HashSet<>(Set.of(score)));
        } else if(lowInProvince.get(province).iterator().next().getMaxScore() == score.getMaxScore()) {
            lowInProvince.get(province).add(score);
        }
        String other = score.getMajor().indexOf("（") != -1 ? score.getMajor().substring(0, score.getMajor().indexOf("（")) : score.getMajor();
        if(!majors.containsKey(other)) {
            majors.put(other, new HashSet<>());
        }
        majors.get(other).add(score.getMajor());
    }

    public String compareWithEach(SchoolScore other) {
        StringBuilder ret = new StringBuilder();
        SchoolScore now = this;
        Set<String> partName = now.makeSameMajorList(other);
        for(String single: partName) {
            ret.append("现在比较的专业为：" + single + "\n");
            for(String with: now.majors.get(single)) {
                List<Double> transfer = now.calculateMajorAverage(with);
                ret.append(now.name + "的" + with + "专业各省平均： 最高分：" + transfer.get(0) 
                    + "最低分：" + transfer.get(1) + "平均分：" + transfer.get(2) + "\n");
            }
            for(String with: other.majors.get(single)) {
                List<Double> transfer = other.calculateMajorAverage(with);
                ret.append(other.name + "的" + with + "专业各省平均： 最高分：" + transfer.get(0) 
                    + "最低分：" + transfer.get(1) + "平均分：" + transfer.get(2) + "\n");
            }
        }
        return ret.toString();
    }

    //all-name
    public List<Double> calculateMajorAverage(String majorName) {
        if(!majorMainMap.containsKey(majorName)) {
                return null;
        }
        double maxRet = 0;
        double minRet = 0;
        double averageRet = 0;
        int num = 0;
        for(String test: majorMainMap.get(majorName).keySet()) {
            if(outOfProvince.contains(test) || outOfProvince.contains(test.substring(0, 2))
                || (test.length() >= 3 && outOfProvince.contains(test.substring(0, 3)))) {
                    continue;
            }
            Score other = majorMainMap.get(majorName).get(test);
            maxRet += other.getMaxScore();
            minRet += other.getMinScore();
            averageRet += other.getAverageScore();
            num++;
        }
        return new ArrayList<Double>(List.of(maxRet / (double)num, minRet / (double)num, averageRet / (double)num));
    }

    //ret: part-name
    public Set<String> makeSameMajorList(SchoolScore other) {
        Set<String> ret = new HashSet<>();
        for(String single: other.majors.keySet()) {
            if(this.majors.containsKey(single)) {
                ret.add(single);
            }
        }
        return ret;
    }

    public void showInformation() {
        System.out.println("学校名为" + name);
        System.out.println("全国最高分之平均分：" + calculateMax());
        System.out.println("全国最低分之平均分：" + calculateMin());
        for(String single: storageMap.keySet()) {
            System.out.println("展示的省份为：" + single);
            for(String other: storageMap.get(single).keySet()) {
                System.out.println("展示的专业为： " + other);
                System.out.println(storageMap.get(single).get(other));
            }
        }
    }

    public String offerInformation() {
        StringBuilder ret = new StringBuilder();
        ret.append("学校名为" + name + "\n");
        ret.append("全国最高分之平均分：" + calculateMax()+ "\n");
        ret.append("全国最低分之平均分：" + calculateMin()+ "\n");
        return ret.toString();
    }

    public double calculateMax() {
        double ret = 0;
        int helper = 0;
        for(Set<Score> single: highInProvince.values()) {
            Score now =  single.iterator().next();
            if(outOfProvince.contains(now.getProvince()) || outOfProvince.contains(now.getProvince().substring(0, 2))
                || (now.getProvince().length() >= 3 && outOfProvince.contains(now.getProvince().substring(0, 3)))) {
                    System.out.println(now.getProvince());
                    continue;
            }
            helper++;
            ret += now.getMaxScore();
        }
        return ret / (double)helper;
    }

    public double calculateMin() {
        double ret = 0;
        int helper = 0;
        for(Set<Score> single: lowInProvince.values()) {
            Score now =  single.iterator().next();
            if(outOfProvince.contains(now.getProvince()) || outOfProvince.contains(now.getProvince().substring(0, 2))
                || (now.getProvince().length() >= 3 && outOfProvince.contains(now.getProvince().substring(0, 3)))) {
                    continue;
            }
            helper++;
            ret += now.getMinScore();
        }
        return ret / (double)helper;
    }
}
