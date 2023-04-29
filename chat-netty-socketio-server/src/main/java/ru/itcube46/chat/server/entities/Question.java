package ru.itcube46.chat.server.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String text;
    private List<String> variants;
    private String answer;
}
