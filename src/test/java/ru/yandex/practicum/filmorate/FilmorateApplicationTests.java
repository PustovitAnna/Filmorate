package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRatings;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
class FilmorateApplicationTests {

	private final FilmService filmService;
	private final UserService userService;
	private Film film;
	private User user;

	@BeforeEach
	void before() {
		film = new Film();
		film.setName("Фильм 1");
		film.setDescription("Описание фильма 1");
		film.setReleaseDate(LocalDate.of(1999, 1, 1));
		film.setDuration(100);
		MpaRatings mpa = new MpaRatings();
		mpa.setId(1);
		mpa.setName("G");
		film.setMpa(mpa);

		user = User.builder()
				.email("mail@mail.ru")
				.login("loginame")
				.name("Nick Name")
				.birthday(LocalDate.of(1985, 4, 4))
				.build();
	}

	@Test
	void contextLoads() {
	}
	@Test
	void testFilmsMethods() {
		user = userService.create(user);
		filmService.create(film);
		List<Film> filmList = (List<Film>) filmService.findAll();
		assertEquals(1, filmList.size(), "Размеры списков не равны");
		assertEquals(1, filmList.get(0).getId(), "Идентификатор не соответствует ожидаемому");

		Film film2 = new Film();
		film2.setId(1);
		film2.setName("Фильм 2");
		film2.setDescription("Описание фильма 2");
		film2.setReleaseDate(LocalDate.of(1998, 1, 1));
		film2.setDuration(150);
		MpaRatings mpa = new MpaRatings();
		mpa.setId(1);
		mpa.setName("G");
		film2.setMpa(mpa);
		Genre genre = new Genre();
		genre.setId(1);
		Genre genre2 = new Genre();
		genre2.setId(3);
		Set<Genre> genres = new HashSet<>();
		genres.add(genre);
		genres.add(genre2);
		film2.setGenres(genres);
		filmService.put(film2);

		filmList = (List<Film>) filmService.findAll();

		assertEquals(1, filmList.size(), "Размеры списков не равны");
		assertEquals(1, filmList.get(0).getId(), "Идентификатор не соответствует ожидаемому");
		assertEquals(filmService.getFilmById(1).getId(), filmList.get(0).getId()
				, "Фильмы не равны");

		Film film3 = new Film();
		film3.setName("Фильм 3");
		film3.setDescription("Описание фильма 3");
		film3.setReleaseDate(LocalDate.of(1999, 1, 1));
		film3.setDuration(100);
		MpaRatings mpa2 = new MpaRatings();
		mpa2.setId(1);
		mpa2.setName("G");
		film3.setMpa(mpa2);
		filmService.create(film3);
		filmService.addLike(2, 1);
		List<Film> films = (List<Film>) filmService.findAll();
		List<Film> popularFilms = filmService.getPopular(2);
		assertEquals(films.size(), popularFilms.size(), "Размеры списков не равны");
		filmService.deleteLike(2, 1);
		popularFilms = filmService.getPopular(2);
		assertEquals(films.size(), popularFilms.size(), "Размеры списков не равны");

		List<User> usersList = (List<User>) userService.findAll();
		assertEquals(1, usersList.size(), "Размер списка не соответствует");
		User user2 = User.builder()
				.id(user.getId())
				.email("mailUpdate@mail.ru")
				.login("Newlogin")
				.name("Nick Name Update")
				.birthday(LocalDate.of(1985, 5, 4))
				.build();
		user = userService.put(user2);
		usersList = (List<User>) userService.findAll();

		assertEquals(1, usersList.size(), "Размер списка пользователей не соответствует ожидаемому");
		assertEquals(user.getId(), usersList.get(0).getId(), "Идентификатор не соответствует ожидаемому");
		assertEquals(userService.getUserById(user.getId()).getId(), usersList.get(0).getId()
				, "Модели User не соответствуют");

		User friend = User.builder()
				.email("mail@yandex.ru")
				.login("loginame")
				.name("Nick Name")
				.birthday(LocalDate.of(1986, 4, 5))
				.build();
		friend = userService.create(friend);
		userService.addFriend(user.getId(), friend.getId());
		List<User> listUser = userService.getFriendList(user.getId());
		List<User> listFriends = userService.getFriendList(friend.getId());
		assertEquals(1, listUser.size(), "Размер списка друзей пользователя не соответствует");

		User user3 = User.builder()
				.email("mail2@yandex.ru")
				.login("loginame2")
				.name("Nick Name2")
				.birthday(LocalDate.of(1986, 4, 5))
				.build();
		user3 = userService.create(user3);

		userService.addFriend(user.getId(), user3.getId());
		userService.addFriend(friend.getId(), user3.getId());
		listUser = userService.getFriendList(user.getId());
		listFriends = userService.getFriendList(friend.getId());
		List<User> listMutualFriends = userService.getListOfMutualFriends(user.getId(), friend.getId());

		assertEquals(2, listUser.size(), "Размер списка друзей пользователя не соответствует");
		assertEquals(1, listFriends.size(), "Размер списка друзей пользователя не соответствуют");
		assertEquals(1, listMutualFriends.size(), "Размер списка общих друзей не равен 1");
		assertEquals(user3.getId(), listMutualFriends.get(0).getId()
				, "Значение в списке общих друзей не соответствует");

		userService.deleteFriend(user.getId(), friend.getId());
		listUser = userService.getFriendList(user.getId());

		assertEquals(1, listUser.size(), "Размер списка друзей пользователя не соответствуют");
		assertEquals(user3.getId(), listUser.get(0).getId(), "Список друзей пользователя после удаления не соответствуют");
	}
}
