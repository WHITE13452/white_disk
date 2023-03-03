package com.whitedisk.white_disk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

/**
 * @author white
 */
@Data
@TableName("music")
public class Music {

    @TableId(type = IdType.AUTO)
    private String musicId;

    private String fileId;

    private String track;

    private String artist;

    private String title;

    private String album;

    private String year;

    private String genre;

    private String comment;

    private String lyrics;

    private String composer;

    private String publicer;

    private String originalArtist;

    private String albumArtist;

    private String copyright;

    private String url;

    private String encoder;

    private String albumImage;

    private Float trackLength;
}
