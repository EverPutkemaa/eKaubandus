package eKaubandus.eKauplus.api.controller;

import eKaubandus.eKauplus.api.dto.request.ProductCreateRequest;
import eKaubandus.eKauplus.api.dto.request.ProductUpdateRequest;
import eKaubandus.eKauplus.api.entity.Product;
import eKaubandus.eKauplus.api.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Tag(name = "Products", description = "Toodete API sõnavarad")
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Operation(summary = "Hangi kõik tooted",
            description = "Tagastab kõik andmebaasis olevad tooted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Hangi toode ID järgi",
            description = "Tagastab toodet valitud ID alusel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Toode leitud"),
            @ApiResponse(responseCode = "404", description = "Toodet ei leitud")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Toote ID", required = true)
            @PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Loo uus toode",
            description = "Loob uue toote andmebaasi (ainult adminitele)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Toode loodud"),
            @ApiResponse(responseCode = "400", description = "Vigased sisendandmed"),
            @ApiResponse(responseCode = "401", description = "Autentimata"),
            @ApiResponse(responseCode = "403", description = "Pole õigusi")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product product = new Product();
        updateProductFromRequest(product, request);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @Operation(summary = "Uuenda toodet",
            description = "Uuendab olemasoleva toote andmeid (ainult adminitele)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Toode uuendatud"),
            @ApiResponse(responseCode = "404", description = "Toodet ei leitud"),
            @ApiResponse(responseCode = "401", description = "Autentimata"),
            @ApiResponse(responseCode = "403", description = "Pole õigusi")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Toote ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        Optional<Product> productOptional = productRepository.findById(id);
        if (!productOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOptional.get();
        updateProductFromRequest(product, request);
        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Kustuta toode",
            description = "Kustutab toote andmebaasist (ainult adminitele)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Toode kustutatud"),
            @ApiResponse(responseCode = "404", description = "Toodet ei leitud"),
            @ApiResponse(responseCode = "401", description = "Autentimata"),
            @ApiResponse(responseCode = "403", description = "Pole õigusi")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "Toote ID", required = true)
            @PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Hangi tooted kategooria järgi",
            description = "Tagastab kõik tooted valitud kategooriaga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @Parameter(description = "Kategooria nimi", required = true)
            @PathVariable String category) {
        List<Product> products = productRepository.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Hangi tooted brändi järgi",
            description = "Tagastab kõik tooted valitud brändiga")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(
            @Parameter(description = "Brändi nimi", required = true)
            @PathVariable String brand) {
        List<Product> products = productRepository.findByBrand(brand);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Hangi uued tooted",
            description = "Tagastab kõik tooted, mis on märgitud kui uued")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping("/new")
    public ResponseEntity<List<Product>> getNewProducts() {
        List<Product> products = productRepository.findByIsNewTrue();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Hangi top 10 enim müüdud toodet",
            description = "Tagastab 10 enim müüdud toodet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping("/top-selling")
    public ResponseEntity<List<Product>> getTopSellingProducts() {
        List<Product> products = productRepository.findTop10ByOrderBySoldCountDesc();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Otsi tooteid",
            description = "Otsib tooteid filtrite alusel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edukalt leitud"),
            @ApiResponse(responseCode = "500", description = "Serveri viga")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @Parameter(description = "Kategooria")
            @RequestParam(required = false) String category,
            @Parameter(description = "Bränd")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Miinimum hind")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maksimum hind")
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<Product> products = productRepository.findWithFilters(category, brand, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Laadi üles toote pilt",
            description = "Laadib üles toote peamise pildi (ainult adminitele)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pilt laaditud üles"),
            @ApiResponse(responseCode = "400", description = "Vale faili formaat"),
            @ApiResponse(responseCode = "404", description = "Toodet ei leitud"),
            @ApiResponse(responseCode = "401", description = "Autentimata"),
            @ApiResponse(responseCode = "403", description = "Pole õigusi")
    })
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductImage(
            @Parameter(description = "Toote ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Pildifail", required = true)
            @RequestParam("file") MultipartFile file) {

        Optional<Product> productOptional = productRepository.findById(id);
        if (!productOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // TODO: Implementeerida pildi üleslaadimine S3-sse
            // String imageUrl = s3Service.uploadFile(file, "products");
            String imageUrl = "https://placeholder-url/" + file.getOriginalFilename();

            Product product = productOptional.get();
            product.setImage(imageUrl);
            productRepository.save(product);

            return ResponseEntity.ok("Pilt laaditud üles edukalt: " + imageUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Pildi üleslaadimine ebaõnnestus: " + e.getMessage());
        }
    }

    // Abiküsimus meetod
    private void updateProductFromRequest(Product product, Object request) {
        ProductCreateRequest createRequest = (ProductCreateRequest) request;
        product.setName(createRequest.getName());
        product.setDescription(createRequest.getDescription());
        product.setPrice(createRequest.getPrice());
        product.setOldPrice(createRequest.getOldPrice());
        product.setCategory(createRequest.getCategory());
        product.setBrand(createRequest.getBrand());
        product.setStock(createRequest.getStock());
        product.setRating(createRequest.getRating());
        product.setReviewCount(createRequest.getReviewCount());
        product.setIsNew(createRequest.getIsNew());
        product.setDiscountPercentage(createRequest.getDiscountPercentage());
        product.setTags(createRequest.getTags());
    }


}
