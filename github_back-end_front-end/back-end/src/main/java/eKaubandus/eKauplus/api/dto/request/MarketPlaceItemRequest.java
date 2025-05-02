package eKaubandus.eKauplus.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class MarketPlaceItemRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    private List<String> images;

    @Size(max = 50)
    private String category;

    @Size(max = 50)
    private String condition;

    @Size(max = 50)
    private String location;

    private String idCardImage;
}
