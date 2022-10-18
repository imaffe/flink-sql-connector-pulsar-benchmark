package com.example.affe.data;

import org.apache.pulsar.shade.com.google.common.base.MoreObjects;

import java.io.Serializable;

public class HuataiUser implements Serializable {
    private static final long serialVersionUID = 1L;
    String name = "";
    int age = 9;
    int sex = 0;
    long createTime ;
    public HuataiUser() {
    }

    public HuataiUser(String name, int age, int sex, long createTime) {
        this.age = age;
        this.name = name;
        this.sex = sex;
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getSex() {
        return sex;
    }
    public long getCreateTime() {
        return createTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", name)
                .add("age", age)
                .add("sex", sex)
                .toString();
    }
}