
package com.example.demo.sis1.library.web.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "\\d{13}", message = "ISBN must contain 13 digits")
    private String isbn;

    @NotNull(message = "Publication year is required")
    @Min(value = 1450)
    private Integer publicationYear;

    @NotNull(message = "Availability is required")
    private Boolean available;
}
