package com.ultrasonic.demo.repository;

import com.ultrasonic.demo.entity.GroupMember;
import com.ultrasonic.demo.entity.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
    
    @Query("SELECT gm FROM GroupMember gm LEFT JOIN FETCH gm.user WHERE gm.groupId = :groupId ORDER BY gm.joinedAt ASC")
    List<GroupMember> findByGroupIdWithUser(@Param("groupId") UUID groupId);

    List<GroupMember> findByGroupId(UUID groupId);
    List<GroupMember> findByUserId(UUID userId);
}
