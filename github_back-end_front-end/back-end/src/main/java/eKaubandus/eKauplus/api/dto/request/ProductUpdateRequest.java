package eKaubandus.eKauplus.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Toote uuendamise päring")
public class ProductUpdateRequest {

    @NotBlank(message = "Toote nimi on kohustuslik")
    @Size(max = 100, message = "Nimi ei tohi olla pikem kui 100 tähemärki")
    @Schema(description = "Toote nimi", example = "iPhone 15 Pro")
    private String name;

    @Size(max = 1000, message = "Kirjeldus ei tohi olla pikem kui 1000 tähemärki")
    @Schema(description = "Toote kirjeldus", example = "Uus iPhone 15 Pro 256GB")
    private String description;

    @NotNull(message = "Hind on kohustuslik")
    @Positive(message = "Hind peab olema positiivne")
    @Schema(description = "Toote hind", example = "999.99")
    private BigDecimal price;

    @Schema(description = "Vana hind (kui on soodustus)", example = "1199.99")
    private BigDecimal oldPrice;

    @NotBlank(message = "Kategooria on kohustuslik")
    @Size(max = 50, message = "Kategooria ei tohi olla pikem kui 50 tähemärki")
    @Schema(description = "Toote kategooria", example = "Telefonid")
    private String category;

    @Size(max = 50, message = "Bränd ei tohi olla pikem kui 50 tähemärki")
    @Schema(description = "Toote bränd", example = "Apple")
    private String brand;

    @PositiveOrZero(message = "Laovaru ei saa olla negatiivne")
    @Schema(description = "Toote laovaru", example = "50")
    private Integer stock;

    @Schema(description = "Toote hinnang (0-5)", example = "4.8")
    private Float rating;

    @Schema(description = "Arvustuste arv", example = "125")
    private Integer reviewCount;

    @Schema(description = "On see uus toode?", example = "true")
    private Boolean isNew;

    @Schema(description = "Allahindlus protsent", example = "15")
    private Integer discountPercentage;

    @Schema(description = "Toote sildid/märksõnad", example = "[\"5G\", \"AI Camera\", \"Pro\"]")
    private List<String> tags;
}
