# edu-exam-svc API 契约

- SERVICE `QuestionService.createQuestion` — 创建题目
  - 请求：QuestionCreateReq
  - 响应：Question
  - 最后更新：2026-05-05

- SERVICE `QuestionService.updateQuestion` — 更新题目
  - 请求：QuestionUpdateReq
  - 响应：Question
  - 最后更新：2026-05-05

- SERVICE `QuestionService.deleteQuestion` — 逻辑删除题目
  - 请求：String id
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `QuestionService.getQuestionById` — 查询题目详情
  - 请求：String id
  - 响应：Question
  - 最后更新：2026-05-05

- SERVICE `QuestionService.listQuestions` — 分页查询题目（支持题库/题型/难度筛选）
  - 请求：QuestionQueryDTO
  - 响应：Page<Question>
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.createPaper` — 创建试卷草稿
  - 请求：ExamPaperCreateReq
  - 响应：ExamPaper
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.updatePaper` — 更新试卷草稿
  - 请求：ExamPaperUpdateReq
  - 响应：ExamPaper
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.deletePaper` — 逻辑删除试卷
  - 请求：String id
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.getPaperById` — 查询试卷详情（含题目列表）
  - 请求：String id
  - 响应：ExamPaperDetailResp
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.listPapers` — 分页查询试卷
  - 请求：ExamPaperQueryDTO
  - 响应：Page<ExamPaper>
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.generatePaper` — 智能组卷（核心算法）
  - 请求：PaperGenerateReq
  - 响应：ExamPaper
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.publishPaper` — 发布试卷
  - 请求：String id
  - 响应：ExamPaper
  - 最后更新：2026-05-05

- SERVICE `ExamPaperService.copyPaper` — 复制试卷及关联题目
  - 请求：String id
  - 响应：ExamPaper
  - 最后更新：2026-05-05

- SERVICE `ExamPaperQuestionService.addQuestionsToPaper` — 批量添加题目到试卷
  - 请求：String paperId, List<PaperQuestionReq>
  - 响应：List<ExamPaperQuestion>
  - 最后更新：2026-05-05

- SERVICE `ExamPaperQuestionService.removeQuestionsFromPaper` — 从试卷移除题目
  - 请求：String paperId, List<String> questionIds
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `ExamPaperQuestionService.reorderQuestions` — 调整试卷题目顺序
  - 请求：String paperId, List<String> questionIdsInOrder
  - 响应：List<ExamPaperQuestion>
  - 最后更新：2026-05-05

- SERVICE `ExamPaperQuestionService.getQuestionsByPaperId` — 查询试卷下题目列表
  - 请求：String paperId
  - 响应：List<ExamPaperQuestion>
  - 最后更新：2026-05-05

- API `POST /api/v1/questions` — 创建题目
  - 请求：QuestionCreateReq
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `GET /api/v1/questions/{id}` — 查询题目详情
  - 请求：PathVariable id
  - 响应：Result<QuestionResp>
  - 最后更新：2026-05-05

- API `PUT /api/v1/questions/{id}` — 更新题目
  - 请求：QuestionUpdateReq
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `DELETE /api/v1/questions/{id}` — 删除题目
  - 请求：PathVariable id
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `GET /api/v1/questions` — 分页查询题目
  - 请求：QuestionQueryDTO
  - 响应：Result<PageResult<QuestionResp>>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-papers` — 创建试卷
  - 请求：ExamPaperCreateReq
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `GET /api/v1/exam-papers/{id}` — 查询试卷详情
  - 请求：PathVariable id
  - 响应：Result<ExamPaperDetailResp>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-papers/{id}/questions` — 批量添加题目到试卷
  - 请求：List<PaperQuestionReq>
  - 响应：Result<Void>
  - 最后更新：2026-05-06

- API `DELETE /api/v1/exam-papers/{id}/questions` — 从试卷移除题目
  - 请求：List<String> questionIds
  - 响应：Result<Void>
  - 最后更新：2026-05-06

- API `PUT /api/v1/exam-papers/{id}/questions/reorder` — 调整试卷题目顺序
  - 请求：List<String> questionIdsInOrder
  - 响应：Result<Void>
  - 最后更新：2026-05-06

- API `PUT /api/v1/exam-papers/{id}` — 更新试卷
  - 请求：ExamPaperUpdateReq
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `DELETE /api/v1/exam-papers/{id}` — 删除试卷
  - 请求：PathVariable id
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `GET /api/v1/exam-papers` — 分页查询试卷
  - 请求：ExamPaperQueryDTO
  - 响应：Result<PageResult<ExamPaperSimpleResp>>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-papers/{id}/publish` — 发布试卷
  - 请求：PathVariable id
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-papers/{id}/copy` — 复制试卷
  - 请求：PathVariable id
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-papers/generate` — 智能组卷
  - 请求：PaperGenerateReq
  - 响应：Result<String>
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.startExam` — 开始考试并创建考试记录/会话
  - 请求：String paperId, Long userId, String ipAddress
  - 响应：String
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.saveAnswer` — 保存单题答案
  - 请求：String recordId, String questionId, String answer
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.submitExam` — 提交考试并触发自动评分
  - 请求：String recordId
  - 响应：void
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.autoGrade` — 自动评分客观题
  - 请求：String recordId
  - 响应：int
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.getExamResult` — 查询考试结果及逐题明细
  - 请求：String recordId
  - 响应：ExamResultResp
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.getPaperExamStats` — 按试卷统计考试参与与通过情况
  - 请求：String paperId
  - 响应：PaperExamStatsResp
  - 最后更新：2026-05-06

- SERVICE `ExamRecordService.listRecords` — 分页查询考试记录
  - 请求：ExamRecordQueryDTO
  - 响应：Page<ExamRecord>
  - 最后更新：2026-05-05

- SERVICE `ExamRecordService.listRecordSummaries` — 分页查询考试记录列表摘要
  - 请求：ExamRecordQueryDTO
  - 响应：Page<ExamRecordListResp>
  - 最后更新：2026-05-05

- SERVICE `ExamSessionService.getSessionByRecordId` — 查询考试会话
  - 请求：String recordId
  - 响应：ExamSession
  - 最后更新：2026-05-05

- SERVICE `ExamSessionService.getSessionStatus` — 查询考试会话运行状态
  - 请求：String recordId
  - 响应：SessionStatusResp
  - 最后更新：2026-05-05

- SERVICE `ExamSessionService.autoSubmitTimeoutExams` — 自动提交超时考试
  - 请求：无
  - 响应：int
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-records/start` — 开始考试
  - 请求：StartExamReq + Header X-User-Id
  - 响应：Result<String>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-records/{recordId}/answers` — 保存答案
  - 请求：SaveAnswerReq
  - 响应：Result<Void>
  - 最后更新：2026-05-05

- API `POST /api/v1/exam-records/{recordId}/submit` — 交卷并返回成绩
  - 请求：PathVariable recordId
  - 响应：Result<ExamResultResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/exam-records/{recordId}/result` — 查询考试结果
  - 请求：PathVariable recordId
  - 响应：Result<ExamResultResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/exam-records/papers/{paperId}/stats` — 按试卷查询考试效果统计
  - 请求：PathVariable paperId
  - 响应：Result<PaperExamStatsResp>
  - 最后更新：2026-05-06

- API `GET /api/v1/exam-records` — 分页查询考试记录
  - 请求：ExamRecordQueryDTO
  - 响应：Result<PageResult<ExamRecordListResp>>
  - 最后更新：2026-05-05

- API `GET /api/v1/exam-sessions/{recordId}/status` — 查询考试会话状态
  - 请求：PathVariable recordId
  - 响应：Result<SessionStatusResp>
  - 最后更新：2026-05-05

- API `GET /api/v1/enums` — 查询考试中心枚举字典
  - 请求：无（前端可附带 `keys` 查询参数，但当前统一返回完整字典）
  - 响应：Result<Map<String, Map<String, String>>>
  - 最后更新：2026-05-05
