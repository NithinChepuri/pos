package com.increff.model.clients;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ClientSearchForm {
    private String name;
    private String email;
} 