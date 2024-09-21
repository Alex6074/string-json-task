package ru.clevertec.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private UUID id;
    private String firstName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd") // only for tests
    private LocalDate dateBirth;

    private List<Order> orders;
}
