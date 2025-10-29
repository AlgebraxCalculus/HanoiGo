package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.BookmarkRequest;
import com.example.hanoiGo.dto.response.BookmarkResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.model.Bookmark;
import com.example.hanoiGo.model.BookmarkList;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.repository.BookmarkRepository;
import com.example.hanoiGo.repository.BookmarkListRepository;
import com.example.hanoiGo.repository.LocationDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkListRepository bookmarkListRepository;
    private final LocationDetailRepository locationDetailRepository;


    @Transactional
    public BookmarkResponse addBookmark(BookmarkRequest request) {
        // Validate bookmark list
        BookmarkList bookmarkList = bookmarkListRepository.findById(request.getBookmarkListId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKMARK_LIST_NOT_FOUND));

        // Kiểm tra quyền sở hữu bookmark list
        if (!bookmarkList.getUser().getId().equals(request.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Validate location
        LocationDetail location = locationDetailRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // Kiểm tra đã bookmark trong list này chưa
        if (bookmarkRepository.existsByBookmarkListIdAndLocationId(
                request.getBookmarkListId(), request.getLocationId())) {
            throw new AppException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        // Tạo bookmark
        Bookmark bookmark = new Bookmark();
        bookmark.setBookmarkList(bookmarkList);
        bookmark.setLocation(location);

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        return mapToResponse(savedBookmark);
    }

    @Transactional
    public void removeBookmark(BookmarkRequest request) {
        // Validate bookmark list
        BookmarkList bookmarkList = bookmarkListRepository.findById(request.getBookmarkListId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKMARK_LIST_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        if (!bookmarkList.getUser().getId().equals(request.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Validate location
        if (!locationDetailRepository.existsById(request.getLocationId())) {
            throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        }

        // Kiểm tra bookmark tồn tại
        if (!bookmarkRepository.existsByBookmarkListIdAndLocationId(
                request.getBookmarkListId(), request.getLocationId())) {
            throw new AppException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        // Xóa bookmark
        bookmarkRepository.deleteByBookmarkListIdAndLocationId(
                request.getBookmarkListId(), request.getLocationId());
    }

    public List<BookmarkResponse> getBookmarkListBookmarks(UUID bookmarkListId, UUID userId) {
        // Validate bookmark list
        BookmarkList bookmarkList = bookmarkListRepository.findById(bookmarkListId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKMARK_LIST_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        if (!bookmarkList.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<Bookmark> bookmarks = bookmarkRepository.findByBookmarkListId(bookmarkListId);

        return bookmarks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookmarkResponse> getAllUserBookmarks(UUID userId) {
        // Lấy tất cả bookmarks của user (qua tất cả bookmark lists)
        List<Bookmark> bookmarks = bookmarkRepository.findAllByUserId(userId);

        return bookmarks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isBookmarked(UUID bookmarkListId, String locationId) {
        return bookmarkRepository.existsByBookmarkListIdAndLocationId(bookmarkListId, locationId);
    }

    public long countBookmarkListBookmarks(UUID bookmarkListId) {
        return bookmarkRepository.countByBookmarkListId(bookmarkListId);
    }

    /**
     * Helper method để convert Bookmark entity sang BookmarkResponse
     */
    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        LocationDetail location = bookmark.getLocation();

        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .locationId(location.getId())
                .locationName(location.getName())
                .locationAddress(location.getAddress())
                .locationDescription(location.getDescription())
                .defaultPicture(location.getDefaultPicture())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .bookmarkedAt(bookmark.getBookmarkedAt())
                .build();
    }
}
