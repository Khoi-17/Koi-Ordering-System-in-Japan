package fall24.swp391.KoiOrderingSystem.controller;


import fall24.swp391.KoiOrderingSystem.enums.ApproveStatus;
import fall24.swp391.KoiOrderingSystem.model.request.QuotationRequest;
import fall24.swp391.KoiOrderingSystem.model.response.QuotationResponse;
import fall24.swp391.KoiOrderingSystem.pojo.Quotations;
import fall24.swp391.KoiOrderingSystem.service.QuotationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quotations")
@CrossOrigin
@SecurityRequirement(name = "api")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    //Create quotations
    @PostMapping("/create")
    public ResponseEntity<QuotationResponse> createQuotation(@RequestBody QuotationRequest quotationRequest) {
        try{
            QuotationResponse quotations = quotationService.createQuotations(quotationRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(quotations);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    //Lấy quotation ? theo quote id
    @GetMapping("/booking/{bookId}")
    public ResponseEntity<List<QuotationResponse>> getQuotations(@PathVariable Long bookId){
        List<QuotationResponse> quotations = quotationService.getQuotationsByBookID(bookId);
        if(quotations.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(quotations);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QuotationResponse>> showAllQuotation(){
        List<QuotationResponse> quotation = quotationService.getAllQuotation();
        if(quotation.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(quotation);
    }

    @GetMapping("/showAllPageable")
    public ResponseEntity<?> showAllPageable(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "5") int size){
        Map<String, Object> response = new HashMap<>();
        response.put("totalPage", quotationService.showAllPageable(page,size).getTotalPages());
        response.put("pageNumber", quotationService.showAllPageable(page,size).getNumber());
        response.put("content", quotationService.showAllPageable(page, size).get());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{quotationId}")
    public ResponseEntity<?> deleteQuotation(@PathVariable Long quotationId){
        try {
            quotationService.deleteQuotations(quotationId);
            return ResponseEntity.ok("Quotation has been deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/admin/{quotationId}")
    public ResponseEntity<QuotationResponse> updateAdminQuotation(
            @PathVariable Long quotationId,
            @RequestParam ApproveStatus approveStatus,
            @RequestBody(required = false) QuotationRequest quotationRequest) {
        try {
            if (quotationRequest == null) {
                quotationRequest = new QuotationRequest();
            }
            QuotationResponse quotations = quotationService.adminUpdateStatusQuotations(quotationId, approveStatus, quotationRequest);
            return ResponseEntity.ok(quotations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/updateAmount/{quotationId}")
    public ResponseEntity<QuotationResponse> updateQuotationAmount(@PathVariable Long quotationId, @RequestBody float amount){
        try{
            QuotationResponse quotations=quotationService.updateAmountQuotations(quotationId, amount);
            return ResponseEntity.ok(quotations);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/updateStatus/{quotationId}")
    public ResponseEntity<QuotationResponse> updateQuotationStatus(@PathVariable Long quotationId, @RequestBody ApproveStatus approveStatus){
        try{
            QuotationResponse quotations=quotationService.updateStatusQuotations(quotationId, approveStatus);
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{quotationId}")
    public ResponseEntity<QuotationResponse> getQuotationById(@PathVariable Long quotationId) {
        try {
            QuotationResponse quotation = quotationService.getQuotationById(quotationId);
            return ResponseEntity.ok(quotation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //Lấy amount từ bookingTourDetail
//    @PutMapping("/{id}/set-amount")
//    public ResponseEntity<?> updateQuotation(@PathVariable Long id){
//        try{
//            quotationService.updateQuotations(id);
//            return ResponseEntity.ok("Amount set successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }


}
