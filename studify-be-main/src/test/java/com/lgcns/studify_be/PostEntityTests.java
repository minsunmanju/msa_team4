// package com.lgcns.studify_be;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;

// import com.lgcns.studify_be.post.domain.dto.PostRequestDTO;
// import com.lgcns.studify_be.post.domain.entity.Category;
// import com.lgcns.studify_be.post.domain.entity.PostEntity;
// import com.lgcns.studify_be.post.repository.PostRepository;

// @SpringBootTest
// @Transactional
// public class PostEntityTests {
    
//     // yaml 변경 필요

//     @Autowired
//     private PostRepository postRepository;

//     @Test
//     public void createPost() {
//         PostRequestDTO request = PostRequestDTO.builder()
//                                                 .title("test-title1")
//                                                 .content("test-content1")
//                                                 .category(Category.STUDY)
//                                                 .build();

//         PostEntity savedPost = postRepository.save(request.toEntity());
//         System.out.println(">>> 저장 완료 : " + savedPost);

//         PostEntity foundPost = postRepository.findById(savedPost.getPostId())
//                         .orElseThrow(() -> new RuntimeException("Post not exists"));

//         System.out.println(">>> 조회 결과 : " + foundPost);

//         List<PostEntity> postList = postRepository.findAll();
//         System.out.println(">>> 포스트 수 : " + postList.size());
//         postList.forEach(System.out::println);
//     }

//     @Test
//     public void updatePost() {
//         PostEntity post = postRepository.save(
//             PostRequestDTO.builder()
//                         .title("before-update-test-title1")
//                         .content("before-update-test-content1")
//                         .category(Category.PROJECT)
//                         .build()
//                         .toEntity()
//         );

//         System.out.println(">>> 저장 데이터 : " + postRepository.findAll());
        
//         post.setTitle("after-update-test-title1");
//         postRepository.save(post);
        
//         PostEntity updatedPost = postRepository.findById(post.getPostId()).get();
//         System.out.println(">>> 수정 결과 : " + updatedPost);
//     }
    
//     @Test
//     public void deletePost() {
//         PostEntity post = postRepository.save(
//             PostRequestDTO.builder()
//             .title("delete-test-title1")
//             .content("delete-test-content1")
//             .category(Category.PROJECT)
//             .build()
//             .toEntity()
//         );
            
//         System.out.println(">>> 저장 데이터 : " + postRepository.findAll());
        
//         postRepository.delete(post);
//         System.out.println(">>> 삭제 결과: " + postRepository.existsById(post.getPostId()));
//         System.out.println(">>> 저장 데이터 : " + postRepository.findAll());
//     }

// }
