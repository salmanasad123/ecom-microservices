package com.ecommerce.user.service;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.models.Address;
import com.ecommerce.user.models.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    //private List<User> userList = new ArrayList<>();

    private long nextId = 1L;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> fetchAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserResponse> userResponseList = userList.stream()
                .map((User user) -> {
                    return mapToUserResponse(user);
                }).collect(Collectors.toList());

        return userResponseList;
    }

    public void addUser(UserRequest userRequest) {
        //user.setId(nextId++);
        User user = new User();
        mapUserReqeustToUser(user, userRequest);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(long id) {

        Optional<User> userOptional = userRepository.findById(id);

        Optional<UserResponse> userResponse = userOptional.map((User user) -> {
            return mapToUserResponse(user);
        });

        return userResponse;
    }

    public boolean updateUser(Long id, UserRequest userRequest) {

        boolean updatedUser = userRepository
                .findById(id)
                .map((User existingUser) -> {
                    mapUserReqeustToUser(existingUser, userRequest);
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);

        return updatedUser;
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setId(String.valueOf(user.getId()));
        userResponse.setLastName(user.getLastName());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setUserRole(user.getUserRole());

        if (user.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setCountry(user.getAddress().getCountry());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setZipcode(user.getAddress().getZipcode());
            addressDTO.setStreet(user.getAddress().getStreet());
            userResponse.setAddressDto(addressDTO);
        }

        return userResponse;
    }

    private void  mapUserReqeustToUser(User user, UserRequest userRequest) {
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());

        if (userRequest.getAddress() != null) {
            Address address = new Address();
            address.setCity(userRequest.getAddress().getCity());
            address.setCountry(userRequest.getAddress().getCountry());
            address.setState(userRequest.getAddress().getState());
            address.setZipCode(userRequest.getAddress().getZipcode());
            address.setStreet(userRequest.getAddress().getStreet());

            user.setAddress(address);
        }
    }
}
