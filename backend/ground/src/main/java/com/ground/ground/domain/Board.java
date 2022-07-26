package com.ground.ground.domain;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Text;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter @Setter
@Table(name = "t_board")
public class Board {

    @Id @GeneratedValue
    @Column(name = "board_id")
    private Long AI;

    @Column(nullable = false)
    private int user_SEQ;

    @Column(nullable = false)
    private int category_SEQ;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 30, nullable = false)
    private String location;

    @Column(nullable = false)
    private boolean b_private;

    @Column(nullable = false)
    private Date reg_dttm;

    @Column(nullable = false)
    private int reg_user_SEQ;

    @Column(nullable = false)
    private Date mod_dttm;

    @Column(nullable = false)
    private int mod_user_SEQ;
}
