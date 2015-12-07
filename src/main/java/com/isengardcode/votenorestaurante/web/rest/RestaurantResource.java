package com.isengardcode.votenorestaurante.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.isengardcode.votenorestaurante.domain.Restaurant;
import com.isengardcode.votenorestaurante.repository.RestaurantRepository;
import com.isengardcode.votenorestaurante.web.rest.util.HeaderUtil;
import com.isengardcode.votenorestaurante.web.rest.dto.RestaurantDTO;
import com.isengardcode.votenorestaurante.web.rest.mapper.RestaurantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Restaurant.
 */
@RestController
@RequestMapping("/api")
public class RestaurantResource {

    private final Logger log = LoggerFactory.getLogger(RestaurantResource.class);

    @Inject
    private RestaurantRepository restaurantRepository;

    @Inject
    private RestaurantMapper restaurantMapper;

    /**
     * POST  /restaurants -> Create a new restaurant.
     */
    @RequestMapping(value = "/restaurants",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) throws URISyntaxException {
        log.debug("REST request to save Restaurant : {}", restaurantDTO);
        if (restaurantDTO.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new restaurant cannot already have an ID").body(null);
        }
        Restaurant restaurant = restaurantMapper.restaurantDTOToRestaurant(restaurantDTO);
        Restaurant result = restaurantRepository.save(restaurant);
        return ResponseEntity.created(new URI("/api/restaurants/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("restaurant", result.getId().toString()))
            .body(restaurantMapper.restaurantToRestaurantDTO(result));
    }

    /**
     * PUT  /restaurants -> Updates an existing restaurant.
     */
    @RequestMapping(value = "/restaurants",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<RestaurantDTO> updateRestaurant(@RequestBody RestaurantDTO restaurantDTO) throws URISyntaxException {
        log.debug("REST request to update Restaurant : {}", restaurantDTO);
        if (restaurantDTO.getId() == null) {
            return createRestaurant(restaurantDTO);
        }
        Restaurant restaurant = restaurantMapper.restaurantDTOToRestaurant(restaurantDTO);
        Restaurant result = restaurantRepository.save(restaurant);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("restaurant", restaurantDTO.getId().toString()))
            .body(restaurantMapper.restaurantToRestaurantDTO(result));
    }

    /**
     * GET  /restaurants -> get all the restaurants.
     */
    @RequestMapping(value = "/restaurants",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        log.debug("REST request to get all Restaurants");
        return restaurantRepository.findAll().stream()
            .map(restaurant -> restaurantMapper.restaurantToRestaurantDTO(restaurant))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * GET  /restaurants/:id -> get the "id" restaurant.
     */
    @RequestMapping(value = "/restaurants/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable Long id) {
        log.debug("REST request to get Restaurant : {}", id);
        return Optional.ofNullable(restaurantRepository.findOne(id))
            .map(restaurantMapper::restaurantToRestaurantDTO)
            .map(restaurantDTO -> new ResponseEntity<>(
                restaurantDTO,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /restaurants/:id -> delete the "id" restaurant.
     */
    @RequestMapping(value = "/restaurants/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        log.debug("REST request to delete Restaurant : {}", id);
        restaurantRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("restaurant", id.toString())).build();
    }

    @RequestMapping(value = "/restaurant/vote/{id}",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> insertVotesById(@PathVariable Long id){
        log.debug("REST insert votes in Restaurant by id : {}", id);
        Restaurant restaurant = restaurantRepository.findOne(id);
        restaurant.setVotes(restaurant.getVotes()+1);
        restaurantRepository.save(restaurant);

        return ResponseEntity.ok().build();
    }
}
