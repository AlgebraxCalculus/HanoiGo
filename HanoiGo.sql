-- DROP DATABASE IF EXISTS "HanoiGo";

CREATE DATABASE "HanoiGo"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en-US'
    LC_CTYPE = 'en-US'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================
-- Users
-- =========================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) Unique NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
	firebase_uid varchar(255) unique,
	profile_picture TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    points INT DEFAULT 0,
	fcm_token text
);


-- =========================
-- Tags
-- =========================
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);
-- =========================
-- Locations_detail
-- =========================
CREATE TABLE location_detail (
    id text PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
	default_picture text,
	address varchar(255) NOT NULL,
	latitude double precision NOT NULL,
	longitude double precision NOT NULL
);

-- =========================
-- Locations_tags
-- =========================
CREATE TABLE location_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tags_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    location_id TEXT REFERENCES location_detail(id) ON DELETE CASCADE
);


-- =========================
-- Checkpoints
-- =========================
CREATE TABLE checkpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    location_id TEXT REFERENCES location_detail(id) ON DELETE CASCADE,
    checked_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Reviews
-- =========================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    location_id TEXT REFERENCES location_detail(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Review_pictures
-- =========================
CREATE TABLE review_pictures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	review_id UUID REFERENCES reviews(id) ON DELETE CASCADE,
	picture Text Not null
);


-- =========================
-- User Likes
-- =========================
CREATE TABLE user_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    review_id UUID REFERENCES reviews(id) ON DELETE CASCADE,
    liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, review_id) -- prevent duplicate likes
);

-- =========================
-- Achievements
-- =========================
CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tier VARCHAR(10)
);

-- =========================
-- User Achievements
-- =========================
CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID REFERENCES achievements(id) ON DELETE CASCADE,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, achievement_id) -- prevent duplicate achievement
);

-- Bookmark Lists
-- =========================
CREATE TABLE bookmark_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

-- =========================
-- Bookmark Lists
-- =========================
CREATE TABLE bookmark_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

-- =========================
-- Bookmark Lists
-- =========================
CREATE TABLE bookmark_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL
);

-- =========================
-- Bookmarks
-- =========================
CREATE TABLE bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bookmark_list_id UUID REFERENCES bookmark_lists(id) ON DELETE CASCADE,
    location_id TEXT REFERENCES location_detail(id) ON DELETE CASCADE,
    bookmarked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (bookmark_list_id, location_id) -- prevent duplicate bookmark
);

-- =========================
-- Password_reset_token
-- =========================
CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
	verified BOOLEAN NOT NULL DEFAULT FALSE
);

select * from password_reset_token;


--===============
-- Rank achievements
INSERT INTO achievements (name, description, tier) VALUES
('Climbing Rookie', 'Reached Top 100', 'A'),
('Rank Riser', 'Reached Top 50', 'S'),
('Elite Climber', 'Reached Top 25', 'S+'),
('Legend on the Rise', 'Reached Top 10', 'SS'),
('The G.O.A.T', 'Reached Rank 1', 'SSS');

-- Point achievements
INSERT INTO achievements (name, description, tier) VALUES
('Point Collector', 'Scored 50 points', 'A'),
('Point Hunter', 'Scored 100 points', 'S'),
('Point Master', 'Scored 200 points', 'S+'),
('Point Slayer', 'Scored 400 points', 'SS'),
('Bro chill...', 'Scored 500 points', 'SSS');

-- Checkpoint achievements
INSERT INTO achievements (name, description, tier) VALUES
('Touched grass', 'Checked in at 1 place', 'A'),
('Pathfinder', 'Checked in at 10 places', 'S'),
('Travel Addict', 'Checked in at 20 places', 'S+'),
('Wanna take a break?', 'Checked in at 30 places', 'SS'),
('Mr Worldwide', 'Checked in at 40 places', 'SSS');

-- Like achievements
INSERT INTO achievements (name, description, tier) VALUES
('Starter Influencer', 'Got 5 likes on a review', 'A'),
('Trending Reviewer', 'Got 10 likes on a review', 'S'),
('Hot Content', 'Got 20 likes on a review', 'S+'),
('Auraaaa', 'Got 30 likes on a review', 'SS'),
('Sir, you won the Internet!', 'Got 50 likes on a review', 'SSS');

-- Achievement collector
INSERT INTO achievements (name, description, tier) VALUES
('Badge Beginner', 'Owned 5 achievements', 'A'),
('Badge Collector', 'Owned 10 achievements', 'S'),
('Badge Hoarder', 'Owned 15 achievements', 'S+'),
('Badge Freak', 'Owned 20 achievements', 'SS'),
('SIUUUUUUUU', 'Owned all achievements', 'SSS');

--===============
-- Tags
INSERT INTO tags (name) VALUES
('Iconic'),
('Cuisine'),
('Entertaining'),
('Culture');

-- Iconic locations
INSERT INTO location_detail 
    (id, name, description, default_picture, address, latitude, longitude) 
VALUES
    (
        'qZ1XUh_vkxlmgWOpfni58F-ETqhJD5SZfLciTrJ6l_p63ztarA6YeWarVpOo0LU-ZrlOUbcOm91lnUINHgla-mk24aRSrUabQf7tNWZtrm_J-gV4NrQ2Lm3ypViabDODb',
        'Hồ Hoàn Kiếm',
        'Hồ Hoàn Kiếm là trái tim của Hà Nội, nổi bật với mặt nước xanh biếc, Tháp Rùa cổ kính và cầu Thê Húc đỏ rực dẫn vào đền Ngọc Sơn. Đây không chỉ là điểm tham quan nổi tiếng mà còn là nơi lưu giữ những giá trị lịch sử, văn hóa và không khí yên bình giữa lòng phố cổ.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759653241/ho_hoan_kiem_mgbnsy.jpg',
        'Đinh Tiên Hoàng, Hàng Trống, Hoàn Kiếm, Hà Nội',
        21.028882673000055,
        105.85260860400007
    ),
    (
        'ypWSmmqN0It71qAoS1-81HaKo1W-Xrvac5o8UaFhlpdE7V8XoAaenUKxSyevY9dfddaeHaUxtcERCi0suorsrd5fUNF6SWRB3drJEUJJikvt3iFcEpASCknWgXy-SBenS',
        'Lăng Bác',
        'Lăng Chủ tịch Hồ Chí Minh là nơi yên nghỉ của Bác, được xây dựng trang nghiêm tại Quảng trường Ba Đình. Đây là điểm đến linh thiêng, thu hút đông đảo du khách trong và ngoài nước.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654177/lang_bac_a5tcsy.jpg',
        'Lăng Bác, Hùng Vương, Điện Biên, Ba Đình, Hà Nội',
         21.0368973,
         105.8346667
    ),
    (
        'PsBMrHjvdta7wiBEZHb0hrakdShXeaiOu7AZxtAZNhsK3mEtmP0ug2yOId2FXX6iqsIF9KWR1haa4mWEmeGa8g4GnfUNiiU9khqZQRGPbhoqwnEMQYRCWhoa0SztjEf3G',
        'Nhà hát lớn Hà Nội',
        'Nhà hát Lớn Hà Nội mang kiến trúc Pháp cổ điển, là công trình nghệ thuật biểu tượng của thủ đô. Đây thường xuyên diễn ra các sự kiện văn hóa, hòa nhạc và biểu diễn nghệ thuật lớn.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654176/Nh%C3%A0_H%C3%A1t_L%E1%BB%9Bn_H%C3%A0_N%E1%BB%99i_njilod.jpg',
        'Nhà hát lớn Hà Nội, 1 Tràng Tiền, Tràng Tiền, Hoàn Kiếm, Hà Nội',
         21.02422325400005,
         105.85764396300004
    ),
    (
        'RS8urUk9d7Nixqk9tC01Zy2my-wKHYE3IZ5iEMbFNTslQxhl__01NgWCxg7HIslWEV3Jlo3OyYdCQxVpGsGGY-1fEmAY',
        'Văn Miếu - Quốc Tử Giám',
        'Văn Miếu – Quốc Tử Giám là trường đại học đầu tiên của Việt Nam, tôn vinh truyền thống hiếu học. Không gian cổ kính, rợp bóng cây tạo nên điểm đến văn hóa – lịch sử đặc sắc.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654176/van_mieu_quoc_tu_giam_vojlbb.jpg',
        'Văn Miếu – Quốc Tử Giám, Đống Đa, Hà Nội',
        21.0281231,
        105.8391386
    ),
    (
        'j6pWLXC6Ao1Gjm8buHuy3HOO9RVeka4eXfdFnVb1fud1FnDIyv2Sx0EbSexCjA5jQaaRjJr5rcc2ctXsCknR2lqbSQVlxc1jqprQwm5RkQv1xjlGUogKElHOmWSmUA-_U',
        'Hoàng thành Thăng Long',
        'Hoàng thành Thăng Long là di sản văn hóa thế giới, từng là trung tâm chính trị suốt nhiều triều đại. Nơi đây lưu giữ dấu ấn nghìn năm lịch sử và kiến trúc cổ độc đáo.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654175/ho%C3%A0ng_th%C3%A0nh_th%C4%83ng_long_rntmwn.jpg',
        'Hoàng Thành Thăng Long, 19C Hoàng Diệu, Điện Biên, Ba Đình, Hà Nội',
        21.034465864000026,
        105.84009629400003
    );

INSERT INTO location_tags (tags_id, location_id)
VALUES 
((SELECT id FROM tags WHERE name = 'Iconic'),
 'qZ1XUh_vkxlmgWOpfni58F-ETqhJD5SZfLciTrJ6l_p63ztarA6YeWarVpOo0LU-ZrlOUbcOm91lnUINHgla-mk24aRSrUabQf7tNWZtrm_J-gV4NrQ2Lm3ypViabDODb'),

((SELECT id FROM tags WHERE name = 'Iconic'),
 'ypWSmmqN0It71qAoS1-81HaKo1W-Xrvac5o8UaFhlpdE7V8XoAaenUKxSyevY9dfddaeHaUxtcERCi0suorsrd5fUNF6SWRB3drJEUJJikvt3iFcEpASCknWgXy-SBenS'),

((SELECT id FROM tags WHERE name = 'Iconic'),
 'PsBMrHjvdta7wiBEZHb0hrakdShXeaiOu7AZxtAZNhsK3mEtmP0ug2yOId2FXX6iqsIF9KWR1haa4mWEmeGa8g4GnfUNiiU9khqZQRGPbhoqwnEMQYRCWhoa0SztjEf3G'),

((SELECT id FROM tags WHERE name = 'Iconic'),
 'RS8urUk9d7Nixqk9tC01Zy2my-wKHYE3IZ5iEMbFNTslQxhl__01NgWCxg7HIslWEV3Jlo3OyYdCQxVpGsGGY-1fEmAY'),

((SELECT id FROM tags WHERE name = 'Iconic'),
 'j6pWLXC6Ao1Gjm8buHuy3HOO9RVeka4eXfdFnVb1fud1FnDIyv2Sx0EbSexCjA5jQaaRjJr5rcc2ctXsCknR2lqbSQVlxc1jqprQwm5RkQv1xjlGUogKElHOmWSmUA-_U');

-- Cuisine locations
INSERT INTO location_detail 
    (id, name, description, default_picture, address, latitude, longitude) 
VALUES
    (
        'loH1fLyxHZJLhEHtenH2F53iEchOcb-6eYIVAEqhQhcJ7gjMorGIKwkuRVN2YCHjhBIVtvJtielBKk9QyqySvZkytSG2RIqvWeb1LX51tnfR4h1gLqwuNnXp_UCCdr5Xm',
        'Phở thìn Bờ Hồ',
        'Phở Thìn Bờ Hồ nổi tiếng với hương vị phở truyền thống đậm đà, nước dùng trong và ngọt tự nhiên. Đây là một trong những quán phở lâu đời được du khách yêu thích khi ghé Hà Nội.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654682/ph%E1%BB%9F_th%C3%ACn_b%E1%BB%9D_h%E1%BB%93_y8b65i.jpg',
        'Phở Thìn Bờ Hồ, 61 Đinh Tiên Hoàng, Lý Thái Tổ, Hoàn Kiếm, Hà Nội',
        21.030252132000044,
        105.85397123000007
    ),
    (
        'JDdMppYgj7hruHoHt0eFim_OZROgQLDrY7toLIVxt89uM3lHgh649GgzqXasHb2IbZNtb6AdiokFukdcjB6CjXSQXy6Lepbsbqp6cSIp6iuNvgk-mvByakG0cR4qKHfHK',
        'Bún chả Hàng Quạt',
        'Bún chả Hàng Quạt được biết đến với thịt nướng thơm lừng, chả viên đậm vị và nước chấm hài hòa. Quán nhỏ nhưng luôn đông khách nhờ hương vị chuẩn Hà Nội.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654682/b%C3%BAn_ch%E1%BA%A3_h%C3%A0ng_qu%E1%BA%A1t_rboegp.jpg',
        'Bún Chả Hàng Quạt, 74 Hàng Quạt, Hàng Gai, Hoàn Kiếm, Hà Nội',
        21.032539476000068,
        105.84878921200004
    ),
    (
        'xusUroKKnrNTonUZqE-Fg2fFW0GEF5r-Zq5tT4pPnodjjEBX8XGShmuuRAK1FI3bY2dXPoVgYNq-a1c7hIJj5GayWEaETrA_ZvxUQPFygqJnmEcUtBSSgEmU7T7CCFfnC',
        'Chả cá Lã Vọng',
        'Chả cá Lã Vọng là món đặc sản trứ danh, với cá được tẩm ướp rồi nướng và xào cùng thì là, hành lá. Đây là trải nghiệm ẩm thực không thể bỏ lỡ khi đến Hà Nội.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654682/ch%E1%BA%A3_c%C3%A1_l%C3%A3_v%E1%BB%8Dng_rn4hhm.jpg',
        'Chả cá Lã Vọng, 14 Chả Cá, Hàng Đào, Hoàn Kiếm, Hà Nội',
        21.035771859000022,
        105.84911299400005
    ),
    (
        'cnKRGCCwX36Bon07v2xv146_vEt-b42MX8ilNrhhhtNflHkLp39s_GlDrwGntHzlWXWiAb1uoOhYKGsBvm-rxpSvaQ6whoptaq5YTI43judrlEvWuBiej2mm8QzOOGfXO',
        'Phở gia truyền Bát Đàn',
        'Phở Bát Đàn nổi tiếng với bát phở đầy đặn, nước dùng đậm vị và thịt bò tươi ngon. Du khách sẵn sàng xếp hàng để thưởng thức hương vị truyền thống chuẩn Hà Nội.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654685/ph%E1%BB%9F_b%C3%A1t_%C4%91%C3%A0n_ioelx0.jpg',
        'Phở Gia Truyền Bát Đàn, 49 P. Bát Đàn, Cửa Đông, Hoàn Kiếm, Hà Nội',
        21.0335288,
        105.8462516
    ),
    (
        'WKNT7kG_5Zd4snRVomC_70egbFy4cZ2Tc4gNfrhundOj1EQ0dgbq8buJVCW9W6_VbKJ5V45dq55GiUwQmQdzKHWiarir2WNQdbFHU5Fhkfh0i1QHpweBkXTajXCyRBurR',
        'Kem Tràng Tiền',
        'Kem Tràng Tiền là thương hiệu kem lâu đời, gắn liền với tuổi thơ của nhiều thế hệ người Hà Nội. Kem có vị mát lạnh, thơm ngậy, đặc biệt là khi thưởng thức ngay tại phố Tràng Tiền.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654682/kem_tr%C3%A0ng_ti%E1%BB%81n_i0trx7.jpg',
        'Kem 33 Phố Tràng Tiền, Tràng Tiền, Hoàn Kiếm, Hà Nội',
        21.0240872,
        105.8547301
    ),
    (
        'uj6ytfBnVN97mmK5s32V5XupTC2fdqPdeN9MBq96lflwq24fnA-7L42d8Q12oaonJ3pFQJa9r4iB83GVJVHiZX0-onhgzeImrfblPW51pmZl8g5YPrw9cmX6riSSZDuLZ',
        'Nhà hàng Bánh tôm Hồ Tây',
        'Bánh tôm Hồ Tây nổi tiếng với tôm chiên vàng giòn, ăn kèm rau sống và nước chấm đậm đà. Nhà hàng ven hồ mang lại không gian thoáng đãng, kết hợp ẩm thực và phong cảnh.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759654682/nh%C3%A0_h%C3%A0ng_b%C3%A1nh_t%C3%B4m_h%E1%BB%93_t%C3%A2y_kqqeoq.jpg',
        'Nhà hàng Bánh Tôm Hồ Tây, Thanh Niên, Trúc Bạch, Ba Đình, Hà Nội',
        21.047336803000064,
        105.83754490800004
    );

INSERT INTO location_tags (tags_id, location_id)
VALUES
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'loH1fLyxHZJLhEHtenH2F53iEchOcb-6eYIVAEqhQhcJ7gjMorGIKwkuRVN2YCHjhBIVtvJtielBKk9QyqySvZkytSG2RIqvWeb1LX51tnfR4h1gLqwuNnXp_UCCdr5Xm'),
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'JDdMppYgj7hruHoHt0eFim_OZROgQLDrY7toLIVxt89uM3lHgh649GgzqXasHb2IbZNtb6AdiokFukdcjB6CjXSQXy6Lepbsbqp6cSIp6iuNvgk-mvByakG0cR4qKHfHK'),
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'xusUroKKnrNTonUZqE-Fg2fFW0GEF5r-Zq5tT4pPnodjjEBX8XGShmuuRAK1FI3bY2dXPoVgYNq-a1c7hIJj5GayWEaETrA_ZvxUQPFygqJnmEcUtBSSgEmU7T7CCFfnC'),
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'cnKRGCCwX36Bon07v2xv146_vEt-b42MX8ilNrhhhtNflHkLp39s_GlDrwGntHzlWXWiAb1uoOhYKGsBvm-rxpSvaQ6whoptaq5YTI43judrlEvWuBiej2mm8QzOOGfXO'),
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'WKNT7kG_5Zd4snRVomC_70egbFy4cZ2Tc4gNfrhundOj1EQ0dgbq8buJVCW9W6_VbKJ5V45dq55GiUwQmQdzKHWiarir2WNQdbFHU5Fhkfh0i1QHpweBkXTajXCyRBurR'),
    ((SELECT id FROM tags WHERE name = 'Cuisine'),
     'uj6ytfBnVN97mmK5s32V5XupTC2fdqPdeN9MBq96lflwq24fnA-7L42d8Q12oaonJ3pFQJa9r4iB83GVJVHiZX0-onhgzeImrfblPW51pmZl8g5YPrw9cmX6riSSZDuLZ');

-- Entertaining locations
INSERT INTO location_detail 
    (id, name, description, default_picture, address, latitude, longitude) 
VALUES
    (
        'jHYoWJJRWZpw03oPUvFm773xwT3eo1VHM0WhcXqCzifVDuUAskEiVp0S5SDHhZ42ka5tiEZO57tNoXzM1vLSZkEW0PwOkWaf9cbVDV5Vllfxwj1ADowOFlXKnLFiVAp3u',
        'Phố đi bộ hồ Hoàn Kiếm',
        'Phố đi bộ quanh Hồ Hoàn Kiếm là không gian văn hóa sôi động vào cuối tuần, nơi diễn ra nhiều hoạt động nghệ thuật đường phố. Đây là điểm vui chơi, thư giãn lý tưởng cho cả người dân và du khách.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656529/pho_di_bo_hoan_kiem_czcbzg.jpg',
        'Phố đi bộ Hồ Hoàn Kiếm, Hàng Trống, Hoàn Kiếm, Hà Nội',
        21.031539517000056,
        105.85107102600006
    ),
    (
        'lVeFE4ZhoAVntH4ehGOJ33WbSDCEZ5nHYbQuCrNnivllnFNBgBGFh6l4SEayE4XNpp15CLURoLd7t3GNgE-sQ2KpXIODequGYaVTPIV1YEiFn-xHsxNgQGK3SDiFEv79F',
        'Tràng Tiền Plaza',
        'Tràng Tiền Plaza là trung tâm thương mại sang trọng bậc nhất Hà Nội, nằm ngay cạnh Hồ Hoàn Kiếm. Nơi đây tập trung nhiều thương hiệu thời trang, mỹ phẩm và hàng xa xỉ nổi tiếng thế giới.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656529/tr%C3%A0ng_ti%E1%BB%81n_plaza_n5nfup.jpg',
        'Tràng Tiền Plaza, Hàng Bài, Tràng Tiền, Hoàn Kiếm, Hà Nội',
        21.024789759000043,
        105.85325051400008
    ),
    (
        'l9pdBw5kRV1lgFFcn0Gi5Ei5VCyvRbbcTrp4W6tvhLR-mn09n3GQhlO4DSmoUabzSalVIZ02h5lmq3h6riHq0X2Ge02mTpzIfLiQqlrxmGh9Kgg6YrlWImH-qDyWYVePY',
        'Bia phố cổ Tạ Hiện',
        'Phố Tạ Hiện được mệnh danh là “phố bia” của Hà Nội, thu hút đông đảo giới trẻ và du khách quốc tế. Không khí sôi động về đêm, kết hợp ẩm thực đường phố và hương vị bia mát lạnh.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656529/pho_bia_ta_hien_wziueu.jpg',
        'Bia Phố Cổ Tạ Hiện, Phố Tạ Hiện, Hàng Buồm, Hoàn Kiếm, Hà Nội',
        21.0347954,
        105.8520727
    ),
    (
        'nE-H7J9qNZ0R5tUA6tU_3zni0aiW0TJXsVKVcAbNmt99TYKkFqxKASKVMw_6sR7dlVZ9ySaxl-txVplA0tRKF-lecfiu2T5k8YaVT7oV1hUdgn0ATsxOVhWK3SDiFihLF',
        'Công viên nước Hồ Tây',
        'Công viên nước Hồ Tây là điểm vui chơi giải trí hấp dẫn với nhiều trò trượt nước, hồ bơi và khu vui chơi hiện đại. Đây là lựa chọn tuyệt vời cho những ngày hè nắng nóng ở thủ đô.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656529/cong_vien_nuoc_ho_tay_ku1yvb.jpg',
        '614 Lạc Long Quân, Nhật Tân, Tây Hồ, Hà Nội',
        21.073172768000063,
        105.81762088400006
    ),
    (
        'cqNI5pS53lJwuWkVvGSv0EO2TC-_XLCadYxcOqNY3YZEpGohomP4s3Gu0WJ6nWTDhR49IM5MBleZ8pW6dp0iJC2mkPv2rderccbVDVwJllfxwlVADjwOFlXJ3WCiVp-7V',
        'Bãi đá sông Hồng',
        'Bãi đá sông Hồng là khu vườn hoa ven sông nổi tiếng, lý tưởng cho chụp ảnh và dã ngoại. Cảnh sắc thiên nhiên lãng mạn, đặc biệt vào mùa hoa bãi bồi nở rộ.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656528/bai_da_song_hong_xgacwz.jpg',
        'Bãi đá Sông Hồng, Nhật Tân, Tây Hồ, Hà Nội',
        21.07823721500006,
        105.83544132400004
    );


INSERT INTO location_tags (tags_id, location_id)
VALUES
    ((SELECT id FROM tags WHERE name = 'Entertaining'),
     'jHYoWJJRWZpw03oPUvFm773xwT3eo1VHM0WhcXqCzifVDuUAskEiVp0S5SDHhZ42ka5tiEZO57tNoXzM1vLSZkEW0PwOkWaf9cbVDV5Vllfxwj1ADowOFlXKnLFiVAp3u'),
    ((SELECT id FROM tags WHERE name = 'Entertaining'),
     'lVeFE4ZhoAVntH4ehGOJ33WbSDCEZ5nHYbQuCrNnivllnFNBgBGFh6l4SEayE4XNpp15CLURoLd7t3GNgE-sQ2KpXIODequGYaVTPIV1YEiFn-xHsxNgQGK3SDiFEv79F'),
    ((SELECT id FROM tags WHERE name = 'Entertaining'),
     'l9pdBw5kRV1lgFFcn0Gi5Ei5VCyvRbbcTrp4W6tvhLR-mn09n3GQhlO4DSmoUabzSalVIZ02h5lmq3h6riHq0X2Ge02mTpzIfLiQqlrxmGh9Kgg6YrlWImH-qDyWYVePY'),
    ((SELECT id FROM tags WHERE name = 'Entertaining'),
     'nE-H7J9qNZ0R5tUA6tU_3zni0aiW0TJXsVKVcAbNmt99TYKkFqxKASKVMw_6sR7dlVZ9ySaxl-txVplA0tRKF-lecfiu2T5k8YaVT7oV1hUdgn0ATsxOVhWK3SDiFihLF'),
    ((SELECT id FROM tags WHERE name = 'Entertaining'),
     'cqNI5pS53lJwuWkVvGSv0EO2TC-_XLCadYxcOqNY3YZEpGohomP4s3Gu0WJ6nWTDhR49IM5MBleZ8pW6dp0iJC2mkPv2rderccbVDVwJllfxwlVADjwOFlXJ3WCiVp-7V');

-- Culture locations
INSERT INTO location_detail 
    (id, name, description, default_picture, address, latitude, longitude) 
VALUES
    (
        'cFe1PAqY7Ay5DjUAXowG76USlQ1CnXrPId7ZMLaJfjfx0WLmdpwOvLHdAXPOiWI7Tc6RAd7xQs_Rrm25qqFyZ4UWpYrm_ZrfQcRNDtZWaZfxwj1ADoyyFlXKnWJWVKO7V',
        'Đền Ngọc Sơn',
        'Đền Ngọc Sơn nằm trên đảo Ngọc giữa Hồ Hoàn Kiếm, thờ các vị anh hùng dân tộc và thần văn học. Đây là công trình kiến trúc cổ kính, mang đậm giá trị tâm linh và văn hóa.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656528/%C4%91%E1%BB%81n_ng%E1%BB%8Dc_s%C6%A1n_mddmvr.jpg',
        'Đền Ngọc Sơn, Lý Thái Tổ, Hoàn Kiếm, Hà Nội',
        21.03072283100005,
        105.85239019300008
    ),
    (
        'p7ZbfFvdptZqQ5FMpc3SnXKaGeTZrX7b_R5lPEqB1uMxAiV8WkwGm3UeWYQ-nZdHqL55DQZdbutJV5U8IoGS15HGseQ-ld7bLcpZAVJZmjP9zhqQAUwCklnHtoCuWnroB',
        'Cầu Thê Húc',
        'Cầu Thê Húc sơn đỏ rực, uốn cong duyên dáng dẫn vào đền Ngọc Sơn. Biểu tượng nổi bật này tượng trưng cho ánh sáng mặt trời, là điểm check-in quen thuộc của du khách.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656528/cau_the_huc_iujuxp.jpg',
        'Cầu Thê Húc, Hồ Hoàn Kiếm, Đinh Tiên Hoàng, Lý Thái Tổ, Hoàn Kiếm, Hà Nội',
        21.03077036700006,
        105.85275319600004
    ),
    (
        'dmyXugLrLXZqtEqnqqYae0SnbxKcpJSlQrV7K-N0mOFloqNAPowOUQXCNQTmbAqOURdC4F7VxjPJ3QY5RpEOY6kWmezuie5jMcLRCVpRklP1xjlECogKElHOmWSmUA-_U',
        'Tháp Bút',
        'Tháp Bút nằm ngay lối vào đền Ngọc Sơn, mang ý nghĩa “tôn vinh tri thức và văn chương”. Công trình là biểu tượng văn hóa, gắn liền với hình ảnh Hồ Hoàn Kiếm.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656529/thap_but_nvwgjx.jpg',
        'Tháp Bút, Đinh Tiên Hoàng, Lý Thái Tổ, Hoàn Kiếm, Hà Nội',
        21.030481726000062,
        105.85341127500004
    ),
    (
        '-m6w59WuXl5uiWtXoHVCx-EGMRhehB5-RdL9GM7pym_Jy11pSpAaQKr9yNZGgWb2hsrFGETYGk5Vt04oPlWFxkkVKWRyjtl922LNFUZNjk3d2iZwFpQVWk3Shmy6TBOiD',
        'Nhà tù Hỏa Lò',
        'Nhà tù Hỏa Lò là di tích lịch sử đặc biệt, từng giam giữ nhiều chiến sĩ cách mạng trong thời kỳ Pháp thuộc. Hiện nay, nơi đây trở thành bảo tàng tái hiện chân thực một phần lịch sử hào hùng của dân tộc.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656528/nha_tu_hoa_lo_ctrncb.jpg',
        'Nhà tù Hỏa Lò, 1 Hỏa Lò, Trần Hưng Đạo, Hoàn Kiếm, Hà Nội',
        21.025350354000068,
        105.84656399900007
    ),
    (
        'YbAHR4n5BJ1HiFJ-kHGJ63SjH0yIY-KSbJAeP6RcmYl3jiQ7oJ5uzkCONmGUzo3tdokRapd0mnNunz5Ep9i9RkCdLkencKHadbFTKJFhkfh0iT1RcpweBkXajLFyRBurR',
        'Nhà hát múa rối Thăng Long',
        'Nhà hát múa rối Thăng Long nổi tiếng với nghệ thuật múa rối nước truyền thống của Việt Nam. Đây là trải nghiệm văn hóa độc đáo, thu hút đông đảo khách du lịch quốc tế.',
        'https://res.cloudinary.com/dsm1uhecl/image/upload/v1759656528/nha_hat_mua_roi_thang_long_monpgz.jpg',
        'Nhà hát Múa rối Thăng Long, 57B Đinh Tiên Hoàng, Hàng Bạc, Hoàn Kiếm, Hà Nội',
        21.03163307500006,
        105.85329818600007
    );

INSERT INTO location_tags (tags_id, location_id)
VALUES
    ((SELECT id FROM tags WHERE name = 'Culture'),
     'cFe1PAqY7Ay5DjUAXowG76USlQ1CnXrPId7ZMLaJfjfx0WLmdpwOvLHdAXPOiWI7Tc6RAd7xQs_Rrm25qqFyZ4UWpYrm_ZrfQcRNDtZWaZfxwj1ADoyyFlXKnWJWVKO7V'),
	 ((SELECT id FROM tags WHERE name = 'Culture'),
     'qZ1XUh_vkxlmgWOpfni58F-ETqhJD5SZfLciTrJ6l_p63ztarA6YeWarVpOo0LU-ZrlOUbcOm91lnUINHgla-mk24aRSrUabQf7tNWZtrm_J-gV4NrQ2Lm3ypViabDODb'),
    ((SELECT id FROM tags WHERE name = 'Culture'),
     'p7ZbfFvdptZqQ5FMpc3SnXKaGeTZrX7b_R5lPEqB1uMxAiV8WkwGm3UeWYQ-nZdHqL55DQZdbutJV5U8IoGS15HGseQ-ld7bLcpZAVJZmjP9zhqQAUwCklnHtoCuWnroB'),
    ((SELECT id FROM tags WHERE name = 'Culture'),
     'dmyXugLrLXZqtEqnqqYae0SnbxKcpJSlQrV7K-N0mOFloqNAPowOUQXCNQTmbAqOURdC4F7VxjPJ3QY5RpEOY6kWmezuie5jMcLRCVpRklP1xjlECogKElHOmWSmUA-_U'),
    ((SELECT id FROM tags WHERE name = 'Culture'),
     '-m6w59WuXl5uiWtXoHVCx-EGMRhehB5-RdL9GM7pym_Jy11pSpAaQKr9yNZGgWb2hsrFGETYGk5Vt04oPlWFxkkVKWRyjtl922LNFUZNjk3d2iZwFpQVWk3Shmy6TBOiD'),
    ((SELECT id FROM tags WHERE name = 'Culture'),
     'YbAHR4n5BJ1HiFJ-kHGJ63SjH0yIY-KSbJAeP6RcmYl3jiQ7oJ5uzkCONmGUzo3tdokRapd0mnNunz5Ep9i9RkCdLkencKHadbFTKJFhkfh0iT1RcpweBkXajLFyRBurR');

select * from location_detail;
select * from checkpoints;
select * from location_tags;
select * from achievements;
select * from users;


delete from
delete from user_achievements
update users u
set points = 0 where u.username = 'SIUUUUUUUU';

ALTER TABLE bookmarks 
DROP CONSTRAINT IF EXISTS bookmarks_user_id_location_id_key;

ALTER TABLE bookmarks 
DROP CONSTRAINT IF EXISTS bookmarks_user_id_fkey,
DROP COLUMN IF EXISTS user_id;

ALTER TABLE bookmarks 
ADD COLUMN bookmark_list_id UUID REFERENCES bookmark_lists(id) ON DELETE CASCADE;

ALTER TABLE bookmarks 
ADD CONSTRAINT bookmarks_unique_list_location UNIQUE (bookmark_list_id, location_id);

select * from bookmarks;

update location_detail
set id = 'UfV-nJQKWWhivJMkomxR1GGKg7wqsCoz-ToJVWJttvsVNvVEZrEGA4E2CrhFkV5DESoadErUIvuN-vGRZmlCy3n28ZwCpUKaweMNKXpx5nCV5hmy8qlmMCnuuUSGcC-fc',
address = 'Hồ Gươm (Hoàn Kiếm), 1-8 P. Lê Thái Tổ, Hàng Trống, Hoàn Kiếm, Hà Nội',
latitude = 21.0284743, longitude = 105.8525795
where name = 'Hồ Hoàn Kiếm'

update location_detail
set id = '0097oNoNX2pzu7ZbsGW0-ExhuzqwUILaV4VKXqgN5eVyqT1YnJKSfwyrVF-q46N7eLk1WZx4I8NNgnW4pFM82EqpTBMqpZZWbfrqVWJpqTPN_gJoMrAyKqH2oVyeaDeGa',
address = 'Văn Miếu – Quốc Tử Giám, 58 P. Quốc Tử Giám, Văn Miếu, Đống Đa, Hà Nội',
latitude = 21.0281175, longitude = 105.8356692
where name = 'Văn Miếu - Quốc Tử Giám'

update location_detail
set id = 'sGFIk2ZOfmFBUxFkDgq86_33EdIODfBjcZ69zDbBgj8Rkxl1Bhk6Phn6_TjKDi49wUp1NQGbwn8WMoi0_aUm12bHGeB2ySJP6saNVQWdzg-qDmUYVtRWTg2SxTj6DFPjD',
address = 'Nhà hát lớn Hà Nội, Tràng Tiền, Hoàn Kiếm, Hà Nội',
latitude = 21.0281175, longitude = 105.8578516
where name = 'Nhà hát lớn Hà Nội'

select * from tags;

delete from location_tags
where location_id = 'qZ1XUh_vkxlmgWOpfni58F-ETqhJD5SZfLciTrJ6l_p63ztarA6YeWarVpOo0LU-ZrlOUbcOm91lnUINHgla-mk24aRSrUabQf7tNWZtrm_J-gV4NrQ2Lm3ypViabDODb';

delete from location_tags
where location_id = 'RS8urUk9d7Nixqk9tC01Zy2my-wKHYE3IZ5iEMbFNTslQxhl__01NgWCxg7HIslWEV3Jlo3OyYdCQxVpGsGGY-1fEmAY';

delete from location_tags
where location_id = 'PsBMrHjvdta7wiBEZHb0hrakdShXeaiOu7AZxtAZNhsK3mEtmP0ug2yOId2FXX6iqsIF9KWR1haa4mWEmeGa8g4GnfUNiiU9khqZQRGPbhoqwnEMQYRCWhoa0SztjEf3G';
