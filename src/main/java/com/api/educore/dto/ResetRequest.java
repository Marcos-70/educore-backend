package com.api.educore.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResetRequest {
    private List<String> modules;
}
