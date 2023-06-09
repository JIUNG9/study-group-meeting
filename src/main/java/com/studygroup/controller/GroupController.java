package com.studygroup.controller;

import com.studygroup.dto.CreateGroupForm;
import com.studygroup.dto.GroupMembersDto;
import com.studygroup.dto.UpdateGroupInfoForm;
import com.studygroup.domain.Member;
import com.studygroup.domain.StudyGroup;
import com.studygroup.enums.MainCategory;
import com.studygroup.exception.ApiError;
import com.studygroup.service.common.CheckDuplicationService;
import com.studygroup.service.group.CreateGroupService;
import com.studygroup.service.group.DeleteGroupService;
import com.studygroup.service.group.FindGroupService;
import com.studygroup.service.group.GroupUpdateNameService;
import com.studygroup.service.group.RetrieveAllGroupsService;
import com.studygroup.service.group.RetrieveGroupsByCategoryService;
import com.studygroup.service.group.RetrieveGroupsByMangedByGroupAdmin;
import com.studygroup.service.group.RetrieveGroupsBySubjectService;
import com.studygroup.service.group.UpdateGroupIntroService;
import com.studygroup.service.group.member.ApplyTheGroupService;
import com.studygroup.service.group.member.RetrieveGroupMembersManagedByGroupAdminService;
import com.studygroup.service.group.member.RetrieveTheNumberOfGroupMemberService;
import com.studygroup.service.user.RetrieveMemberByAuthPrinciple;
import com.studygroup.dto.GroupEntityToGroupInfoDto;
import com.studygroup.dto.GroupInfoDto;
import com.studygroup.util.convertor.ObjectToLong;
import com.studygroup.util.error.ErrorCode;
import com.studygroup.util.constant.GroupAdminIntro;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GroupController {

  private static final Logger logger = LoggerFactory
      .getLogger(GroupController.class);
  private final UpdateGroupIntroService updateGroupIntroService;
  private final RetrieveTheNumberOfGroupMemberService retrieveTheNumberOfGroupMemberService;
  private final RetrieveAllGroupsService retrieveAllGroupsService;
  private final DeleteGroupService deleteGroupService;
  private final RetrieveMemberByAuthPrinciple retrieveMemberByAuthPrinciple;
  private final RetrieveGroupsBySubjectService retrieveGroupsBySubjectService;
  private final FindGroupService findGroupService;
  private final RetrieveGroupsByCategoryService retrieveGroupsByCategoryService;
  private final GroupUpdateNameService updateGroupNameService;
  private final CreateGroupService createGroupService;
  private final CheckDuplicationService checkGroupNameDuplicationService;
  private final ApplyTheGroupService initialGroupMemberAsAdminService;
  private final RetrieveGroupsByMangedByGroupAdmin retrieveGroupsByMangedByGroupAdmin;
  private final RetrieveGroupMembersManagedByGroupAdminService retrieveGroupMembersManagedByGroupAdminService;

  public GroupController(UpdateGroupIntroService updateGroupIntroService,
      RetrieveTheNumberOfGroupMemberService retrieveTheNumberOfGroupMemberService,
      RetrieveAllGroupsService retrieveAllGroupsService,
      DeleteGroupService deleteGroupService,
      RetrieveMemberByAuthPrinciple retrieveMemberByAuthPrinciple,
      RetrieveGroupsBySubjectService retrieveGroupsBySubjectService,
      FindGroupService findGroupService,
      @Qualifier("RetrieveGroupsByCategoryServiceImpl") RetrieveGroupsByCategoryService retrieveGroupsByCategoryService,
      GroupUpdateNameService updateGroupNameService,
      CreateGroupService createGroupService,
      @Qualifier("CheckGroupNameDuplicationService") CheckDuplicationService checkGroupNameDuplicationService,
      @Qualifier("InitialGroupMemberAsAdminService") ApplyTheGroupService initialGroupMemberAsAdminService,
      RetrieveGroupsByMangedByGroupAdmin retrieveGroupsByMangedByGroupAdmin,
      RetrieveGroupMembersManagedByGroupAdminService retrieveGroupMembersManagedByGroupAdminService) {
    this.updateGroupIntroService = updateGroupIntroService;
    this.retrieveTheNumberOfGroupMemberService = retrieveTheNumberOfGroupMemberService;
    this.retrieveAllGroupsService = retrieveAllGroupsService;
    this.deleteGroupService = deleteGroupService;
    this.retrieveMemberByAuthPrinciple = retrieveMemberByAuthPrinciple;
    this.retrieveGroupsBySubjectService = retrieveGroupsBySubjectService;
    this.findGroupService = findGroupService;
    this.retrieveGroupsByCategoryService = retrieveGroupsByCategoryService;
    this.updateGroupNameService = updateGroupNameService;
    this.createGroupService = createGroupService;
    this.checkGroupNameDuplicationService = checkGroupNameDuplicationService;
    this.initialGroupMemberAsAdminService = initialGroupMemberAsAdminService;
    this.retrieveGroupsByMangedByGroupAdmin = retrieveGroupsByMangedByGroupAdmin;
    this.retrieveGroupMembersManagedByGroupAdminService = retrieveGroupMembersManagedByGroupAdminService;
  }


  @PostMapping("/api/groups")
  public ResponseEntity<Object> createGroup(@Valid @RequestBody CreateGroupForm createGroupForm) {
    Object memberId =
        SecurityContextHolder.
            getContext().
            getAuthentication().
            getPrincipal();
    logger.info(memberId.toString());

    if (!checkGroupNameDuplicationService.isDuplicated(createGroupForm.getName())) {
      StudyGroup studyGroup = createGroupService.create(createGroupForm);
      initialGroupMemberAsAdminService.apply(
          retrieveMemberByAuthPrinciple.getMember(ObjectToLong.convert(memberId)),
          studyGroup, createGroupForm.getNickName(),
          GroupAdminIntro.INTRO_AS_ADMIN);

      return ResponseEntity.
          status(HttpStatus.CREATED).
          body("Successfully create the group!");
    }
    return ApiError.buildApiError(
        ErrorCode.GROUP_NAME_IS_DUPLICATED,
        HttpStatus.BAD_REQUEST);


  }

  @GetMapping("/api/groups/admins")
  public ResponseEntity<Object> getGroupsManagedByGroupAdmin() {
    Object memberId =
        SecurityContextHolder.
            getContext().
            getAuthentication().
            getPrincipal();

    Member adminMember =
        retrieveMemberByAuthPrinciple.
            getMember(ObjectToLong.convert(memberId));

    List<GroupInfoDto> groupListManagedByRequestMember =
        retrieveGroupsByMangedByGroupAdmin.
            get(adminMember);

    if (groupListManagedByRequestMember.size() == 0) {
      return ResponseEntity.
          status(HttpStatus.NO_CONTENT).
          build();
    }
    return ResponseEntity.
        status(HttpStatus.OK).
        body(groupListManagedByRequestMember);

  }

  @GetMapping("/api/groups/{groupName}/admins/members")
  public ResponseEntity<Object> getGroupMembersManagedByGroupAdmin(@PathVariable String groupName) {

    Object memberId =
        SecurityContextHolder.
            getContext().
            getAuthentication().
            getPrincipal();

    Member adminMember =
        retrieveMemberByAuthPrinciple.
            getMember(ObjectToLong.convert(memberId));

    StudyGroup studyGroup =
        findGroupService.
            getGroup(groupName);

    List<GroupMembersDto> groupMembersDtoList =
        retrieveGroupMembersManagedByGroupAdminService.get(studyGroup, adminMember);

    if (groupMembersDtoList.size() <= 1) {
      return ResponseEntity.
          status(HttpStatus.NO_CONTENT).
          build();
    }

    return ResponseEntity.
        status(HttpStatus.OK).
        body(groupMembersDtoList);
  }


  @PutMapping("/api/groups/{groupName}/admins/name/{newName}")
  public ResponseEntity<Object> updateGroupName(
      @PathVariable String groupName,
      @PathVariable String newName) {

    StudyGroup studyGroup = findGroupService.getGroup(groupName);
    if (!checkGroupNameDuplicationService.isDuplicated(newName)) {
      updateGroupNameService.update(studyGroup, newName);
      return ResponseEntity.
          status(HttpStatus.OK).
          body("update group name is succeeded ");

    }
    return ApiError.
        buildApiError(
            ErrorCode.GROUP_NAME_IS_DUPLICATED,
            HttpStatus.BAD_REQUEST);
  }


  @PutMapping("/api/groups/{groupName}/admins/intro")
  public ResponseEntity<Object> updateGroupInfo(
      @PathVariable String groupName,
      @Valid @RequestBody UpdateGroupInfoForm updateGroupInfoForm
  ) {
    StudyGroup studyGroup = findGroupService.getGroup(groupName);
    updateGroupIntroService.update(studyGroup, updateGroupInfoForm.getIntro());

    return ResponseEntity.
        status(HttpStatus.OK).
        body("updating group intro is succeeded");

  }


  @GetMapping("/api/groups")
  public ResponseEntity<Object> findGroup(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) MainCategory mainCategory,
      @RequestParam(required = false) String subject) {

    List<GroupInfoDto> groupInfoDtoList = new ArrayList<>();
    if (name == null && mainCategory == null && subject == null) {
      //전체 검색
      groupInfoDtoList = GroupEntityToGroupInfoDto.convert(retrieveAllGroupsService.getAll());
    } else if (name != null) {
      //이름 검색
      groupInfoDtoList.add(GroupEntityToGroupInfoDto.convert(findGroupService.getGroup(name)));
    } else if (mainCategory != null && subject == null) {
      //카테고리 별 검색
      groupInfoDtoList = GroupEntityToGroupInfoDto.convert(
          retrieveGroupsByCategoryService.get(mainCategory));
    } else {
      //주제 검색
      groupInfoDtoList = GroupEntityToGroupInfoDto.convert(
          retrieveGroupsBySubjectService.get(subject));
    }

    if (groupInfoDtoList.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    return ResponseEntity.status(HttpStatus.OK).body(groupInfoDtoList);
  }

  @DeleteMapping("/api/groups/{groupName}/admins")
  public ResponseEntity<Object> deleteGroup(@PathVariable String groupName) {

    StudyGroup studyGroup = findGroupService.getGroup(groupName);
    int groupMemberNumber = retrieveTheNumberOfGroupMemberService.getTheNumberOfGroupMember(
        studyGroup);
    if (groupMemberNumber <= 1) {
      deleteGroupService.delete(studyGroup);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    return ApiError.buildApiError(ErrorCode.CAN_NOT_DELETE_GROUP, HttpStatus.BAD_REQUEST);


  }
}
