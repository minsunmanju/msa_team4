package com.lgcns.studify_be.application.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lgcns.studify_be.application.domain.dto.ApplicationResponseDTO;
import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;
import com.lgcns.studify_be.application.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("studify/api/v1/applications")
public class ApplicationCtrl {
    
    @Autowired
    private ApplicationService applicationService;

    @Operation(summary = "모집 지원", security = { @SecurityRequirement(name = "bearerAuth") })
    @PostMapping("/post/{postId}")
    public ResponseEntity<?> apply(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ApplicationResponseDTO response = applicationService.apply(postId, email);
        if( response != null ) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getApplicationId());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "모집 승인", security = { @SecurityRequirement(name = "bearerAuth") })
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<?> approve(
        @PathVariable Long applicationId,
        @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ApplicationResponseDTO response = applicationService.approve(applicationId, email);
        if( response != null ) {
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "모집 거절", security = { @SecurityRequirement(name = "bearerAuth") })
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<ApplicationEntity> reject(
        @PathVariable Long applicationId,
        @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ApplicationResponseDTO response = applicationService.reject(applicationId, email);
        if( response != null ) {
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}





// package com.lgcns.studify_be.application.ctrl;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.web.bind.annotation.PatchMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;
// import com.lgcns.studify_be.application.service.ApplicationService;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// @RestController
// @RequestMapping("studify/api/v1/applications") // PostCtrl과 동일하게 '/' 제거
// public class ApplicationCtrl {
    
//     @Autowired
//     private ApplicationService applicationService;

//     // 디버깅을 위한 간단한 테스트 엔드포인트 추가
//     @Operation(summary = "Application 테스트", security = { @SecurityRequirement(name = "bearerAuth") })
//     @PostMapping("/test")
//     public ResponseEntity<?> test(@AuthenticationPrincipal UserDetails userDetails) {
//         String email = userDetails.getUsername();
//         System.out.println("Application Test - Email: " + email); // 로그 출력
//         return ResponseEntity.ok("Application endpoint working for: " + email);
//     }

//     @Operation(summary = "스터디 모집글 지원", security = { @SecurityRequirement(name = "bearerAuth") })
//     @PostMapping("/post/{postId}")
//     public ResponseEntity<?> apply(
//             @PathVariable Long postId,
//             @AuthenticationPrincipal UserDetails userDetails) {
        
//         System.out.println("Apply called - PostId: " + postId); // 디버깅 로그
        
//         if (userDetails == null) {
//             System.out.println("UserDetails is null!"); // 디버깅 로그
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
//         }
        
//         String email = userDetails.getUsername();
//         System.out.println("Apply - Email: " + email); // 디버깅 로그
        
//         try {
//             ApplicationEntity result = applicationService.apply(postId, email);
//             if (result != null) {
//                 return ResponseEntity.status(HttpStatus.CREATED).body("Application successful");
//             } else {
//                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Application failed");
//             }
//         } catch (Exception e) {
//             System.out.println("Apply error: " + e.getMessage()); // 디버깅 로그
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
//         }
//     }

//     @Operation(summary = "지원 승인", security = { @SecurityRequirement(name = "bearerAuth") })
//     @PatchMapping("/{applicationId}/approve")
//     public ResponseEntity<ApplicationEntity> approve(@PathVariable Long applicationId) {
//         return ResponseEntity.ok(applicationService.approve(applicationId));
//     }

//     @Operation(summary = "지원 거부", security = { @SecurityRequirement(name = "bearerAuth") })
//     @PatchMapping("/{applicationId}/reject")
//     public ResponseEntity<ApplicationEntity> reject(@PathVariable Long applicationId) {
//         return ResponseEntity.ok(applicationService.reject(applicationId));
//     }
// }