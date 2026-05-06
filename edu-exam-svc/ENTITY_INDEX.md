# edu-exam-svc 实体索引

- Question (bankId, type, content, options, answer, analysis, difficulty, knowledgePoint, score + BaseEntity字段)
  - 用途：题库存储单题题干、答案、选项 JSON 和难度信息
  - 关联表：edu_exam_question
  - 最后更新：2026-05-05

- ExamPaper (title, description, totalScore, duration, passScore, status, type, allowRedo, knowledgeConfig + BaseEntity字段)
  - 用途：存储试卷元数据、总分、考试规则和随机组卷配置
  - 关联表：edu_exam_paper
  - 最后更新：2026-05-05

- ExamPaperQuestion (paperId, questionId, sort, score + BaseEntity字段)
  - 用途：维护试卷与题目的关联关系，并保存排序与试卷内覆盖分值
  - 关联表：edu_exam_paper_question
  - 最后更新：2026-05-05

- ExamRecord (examId, paperId, userId, deptId, totalScore, obtainScore, status, startTime, submitTime, ipAddress, switchCount, answers + BaseEntity字段)
  - 用途：保存考生一次考试作答、交卷和评分结果，是考试结果的主事实表
  - 关联表：edu_exam_record
  - 最后更新：2026-05-05

- ExamSession (recordId, examId, paperId, userId, status, startTime, endTime, submitTime, lastActiveTime, ipAddress + BaseEntity字段)
  - 用途：维护考试中的运行态会话、超时自动交卷和最近活跃时间
  - 关联表：edu_exam_session
  - 最后更新：2026-05-05
