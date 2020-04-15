package com.kk.pojo;

/*
@author kzj
@date 2020/4/14 - 23:21
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    private String title;
    private String img;
    private String price;
}
