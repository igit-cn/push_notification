package com.yidian.push.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by yidianadmin on 15-2-2.
 */
@Getter
@Setter
public class ProcessorConfig {
    private int iPhonePoolSize = 20;
    private int androidPoolSize = 200;
}
