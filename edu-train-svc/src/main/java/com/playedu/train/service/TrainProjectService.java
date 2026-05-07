package com.playedu.train.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playedu.train.domain.entity.TrainProject;
import com.playedu.train.domain.entity.TrainTask;
import com.playedu.train.dto.query.TrainProjectQueryDTO;
import com.playedu.train.dto.req.TrainProjectCreateReq;
import com.playedu.train.dto.req.TrainTaskReq;
import com.playedu.train.dto.resp.TrainProjectDetailResp;
import com.playedu.train.dto.resp.TrainProjectListResp;
import com.playedu.train.dto.resp.ProjectStatsResp;
import com.playedu.train.dto.resp.TrainProjectMyDetailDTO;
import java.util.List;

public interface TrainProjectService {
    TrainProject createProject(Long creatorId, TrainProjectCreateReq req);

    List<TrainTask> addTasks(String projectId, List<TrainTaskReq> tasks);

    void publishProject(String projectId);

    TrainProjectDetailResp getProjectDetail(String projectId);

    TrainProjectMyDetailDTO getMyProjectDetail(String projectId, Long userId);

    ProjectStatsResp getProjectStats(String projectId);

    Page<TrainProjectListResp> listProjects(TrainProjectQueryDTO query);
}
