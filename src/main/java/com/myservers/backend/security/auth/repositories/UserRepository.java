package com.myservers.backend.security.auth.repositories;

import com.myservers.backend.security.auth.entities.Role;
import com.myservers.backend.security.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

   Optional<User> findByEmail(String email);

   List<User> findByState(boolean state);

   @Transactional
   @Modifying
   @Query("update User u set u.state = ?1 where u.id = ?2")
   int updateStateById(boolean state, Integer id);

   int countByRole(String role);


   @Override
   Optional<User> findById(Integer integer);

   @Transactional
   @Modifying
   @Query("update User u set u.role = ?1 where u.id = ?2")
   int updateRoleById(Role role, Integer id);

   @Transactional
   @Modifying
   @Query("update User u set u.password = ?1 where u.id = ?2")
   int updatePasswordById(String password, Integer id);

   @Transactional
   @Modifying
   @Query("update User u set  u.balance = ?1 where u.id = ?2")
   int updateBalanceById(float balance, Integer id);
   @Transactional
   @Modifying
   @Query("update User u set u.telephone = ?1 where u.id = ?2")
   int updateTelephoneById(int telephone, Integer id);





   @Query("select u from User u where u.email = ?1 or u.role = ?2 or u.telephone = ?3")
   List<User> findByEmailOrRoleOrTelephone(String email, Role role, int telephone);

   /*@Query("select u from User u where u.email = ?1 or u.role = ?2")
   List<User> findByEmailOrRole(String email, Role role);

   @Query("SELECT u FROM User u WHERE u.email LIKE %:searchText% " +
           "OR u.role LIKE %:searchText%")
   List<User> searchBysearchText(@Param("searchText") String searchText);*/

   @Query("SELECT u FROM User u WHERE u.email LIKE %:searchText% and u.state= true")
   List<User> searchBysearchTextmail(@Param("searchText") String searchText);



   Optional<User> findByPasswordResetToken(String passwordResetToken);

   List<User> findByRoleAndState(Role role, boolean state);

   @Query("SELECT COUNT(u) FROM User u WHERE u.role=com.myservers.backend.security.auth.entities.Role.USER and  u.date_creation <= :endDate")
   long countUsersCreatedBeforeOrIn(@Param("endDate") Date endDate);

   @Query("SELECT COALESCE(SUM(u.balance), 0) FROM User u WHERE u.date_creation <= :endDate AND u.state = true AND u.role = com.myservers.backend.security.auth.entities.Role.USER")
   double getCumulativeBalanceBeforeOrIn(@Param("endDate") Date endDate);

      // User Group related methods (ManyToMany relationship)
   @Query("SELECT u FROM User u JOIN u.userGroups ug WHERE ug = :userGroup AND u.state = true")
   List<User> findByUserGroupsContaining(@Param("userGroup") com.myservers.backend.users.entities.UserGroup userGroup);

}
