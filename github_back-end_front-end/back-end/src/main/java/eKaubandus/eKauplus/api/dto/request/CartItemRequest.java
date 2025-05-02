package eKaubandus.eKauplus.api.dto.request;


import eKaubandus.eKauplus.api.entity.CartItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull
    private CartItem.ItemType itemType;

    @NotNull
    private Long itemId;

    @NotNull
    @Positive
    private Integer quantity;
}
