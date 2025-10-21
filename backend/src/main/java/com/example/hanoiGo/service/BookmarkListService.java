package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.BookmarkListRequest;
import com.example.hanoiGo.dto.response.BookmarkListResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.model.BookmarkList;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.BookmarkListRepository;
import com.example.hanoiGo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkListService {

    private final BookmarkListRepository bookmarkListRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookmarkListResponse createBookmarkList(BookmarkListRequest request) {
        // Validate user
        User user = userRepository.findUserById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra tên đã tồn tại chưa
        if (bookmarkListRepository.existsByUserIdAndName(request.getUserId(), request.getName())) {
            throw new AppException(ErrorCode.BOOKMARK_LIST_ALREADY_EXISTS);
        }

        // Tạo bookmark list
        BookmarkList bookmarkList = new BookmarkList();
        bookmarkList.setUser(user);
        bookmarkList.setName(request.getName());

        BookmarkList saved = bookmarkListRepository.save(bookmarkList);

        return mapToResponse(saved);
    }

    public List<BookmarkListResponse> getUserBookmarkLists(UUID userId) {
        return bookmarkListRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBookmarkList(UUID listId, UUID userId) {
        BookmarkList bookmarkList = bookmarkListRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKMARK_LIST_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        if (!bookmarkList.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        bookmarkListRepository.delete(bookmarkList);
    }

    private BookmarkListResponse mapToResponse(BookmarkList bookmarkList) {
        return BookmarkListResponse.builder()
                .id(bookmarkList.getId())
                .name(bookmarkList.getName())
                .bookmarkCount(bookmarkList.getBookmarks() != null ? bookmarkList.getBookmarks().size() : 0)
                .build();
    }
}
