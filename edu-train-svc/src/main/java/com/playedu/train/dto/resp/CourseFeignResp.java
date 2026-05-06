package com.playedu.train.dto.resp;

import lombok.Data;

@Data
public class CourseFeignResp {
    private Integer id;

    private String title;

    private Integer thumb;

    private Integer charge;

    private Integer classHour;

    private Integer isRequired;

    private Integer isShow;
}
