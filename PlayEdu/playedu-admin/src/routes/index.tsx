import { lazy } from "react";
import { RouteObject } from "react-router-dom";
import { login, system } from "../api";

import { getToken } from "../utils";
import KeepAlive from "../compenents/keep-alive";
// 页面加载
import InitPage from "../pages/init";
import LoginPage from "../pages/login";
import WithHeaderWithoutFooter from "../pages/layouts/with-header-without-footer";
import WithoutHeaderWithoutFooter from "../pages/layouts/without-header-without-footer";

//首页
const DashboardPage = lazy(() => import("../pages/dashboard"));
//修改密码页面
const ChangePasswordPage = lazy(() => import("../pages/change-password"));
//资源管理相关
const ResourceCategoryPage = lazy(
  () => import("../pages/resource/resource-category")
);
const ResourceImagesPage = lazy(() => import("../pages/resource/images"));
const ResourceVideosPage = lazy(() => import("../pages/resource/videos"));
const ResourceCoursewarePage = lazy(
  () => import("../pages/resource/courseware")
);
//课程相关
const CoursePage = lazy(() => import("../pages/course/index"));
const CourseUserPage = lazy(() => import("../pages/course/user"));
const CourseListPage = lazy(() => import("../pages/course/CourseList"));
const CourseFormPage = lazy(() => import("../pages/course/CourseForm"));
//考试相关
const ExamPaperListPage = lazy(() => import("../pages/exam/ExamPaperList"));
const ExamPaperFormPage = lazy(() => import("../pages/exam/ExamPaperForm"));
const ExamPaperComposePage = lazy(() => import("../pages/exam/ExamPaperCompose"));
const QuestionListPage = lazy(() => import("../pages/exam/QuestionList"));
//培训相关
const TrainProjectListPage = lazy(() => import("../pages/train/TrainProjectList"));
const TrainProjectFormPage = lazy(() => import("../pages/train/TrainProjectForm"));
const TrainProjectDetailPage = lazy(() => import("../pages/train/TrainProjectDetail"));
const TrainTaskConfigPage = lazy(() => import("../pages/train/TrainTaskConfig"));
//直播相关
const LiveRoomListPage = lazy(() => import("../pages/live/LiveRoomList"));
const LiveRoomFormPage = lazy(() => import("../pages/live/LiveRoomForm"));
//积分相关
const PointRuleManagePage = lazy(() => import("../pages/point/PointRuleManage"));
const PointProductManagePage = lazy(
  () => import("../pages/point/PointProductManage")
);
const PointOrderManagePage = lazy(() => import("../pages/point/PointOrderManage"));
//学员相关
const MemberPage = lazy(() => import("../pages/member"));
const MemberImportPage = lazy(() => import("../pages/member/import"));
const MemberLearnPage = lazy(() => import("../pages/member/learn"));
const MemberDepartmentProgressPage = lazy(
  () => import("../pages/member/departmentUser")
);
//系统相关
const SystemConfigPage = lazy(() => import("../pages/system/config"));
const SystemAdministratorPage = lazy(
  () => import("../pages/system/administrator")
);
const SystemAdminrolesPage = lazy(() => import("../pages/system/adminroles"));
const SystemLogPage = lazy(() => import("../pages/system/adminlog"));
//部门页面
const DepartmentPage = lazy(() => import("../pages/department"));
//测试
const TestPage = lazy(() => import("../pages/test"));
//错误页面
const ErrorPage = lazy(() => import("../pages/error"));
//使用许可页面
const LicensingPage = lazy(() => import("../pages/licensing/index"));

import PrivateRoute from "../compenents/private-route";

// const LoginPage = lazy(() => import("../pages/login"));

const LOCAL_DEV_BYPASS = import.meta.env.VITE_LOCAL_DEV_BYPASS === "true";

const localDevLoginData = {
  user: {
    id: 0,
    name: "本地联调用户",
    email: "local-dev@playedu.test",
  },
  permissions: ["*"],
};

const localDevConfigData = {
  "ldap-enabled": false,
  "system.name": "PlayEdu Admin Local",
  "system.logo": "",
  "system.pc_url": "",
  "system.h5_url": "",
  resource_url: {},
  "member.default_avatar": "",
  "default.course_thumbs": [],
  departments: [],
  resource_categories: [],
};

let RootPage: any = null;
if (LOCAL_DEV_BYPASS) {
  RootPage = (
    <InitPage
      configData={localDevConfigData}
      loginData={localDevLoginData}
    />
  );
} else if (getToken()) {
  RootPage = lazy(async () => {
    return new Promise<any>(async (resolve) => {
      try {
        let configRes: any = await system.getSystemConfig();
        let userRes: any = await login.getUser();

        resolve({
          default: (
            <InitPage configData={configRes.data} loginData={userRes.data} />
          ),
        });
      } catch (e) {
        console.error("系统初始化失败", e);
        resolve({
          default: <ErrorPage />,
        });
      }
    });
  });
} else {
  RootPage = <InitPage />;
}

const routes: RouteObject[] = [
  {
    path: "/",
    element: RootPage,
    children: [
      {
        path: "/",
        element: <PrivateRoute Component={<WithHeaderWithoutFooter />} />,
        children: [
          {
            path: "/",
            element: <PrivateRoute Component={<DashboardPage />} />,
          },
          {
            path: "/change-password",
            element: <PrivateRoute Component={<ChangePasswordPage />} />,
          },
          {
            path: "/resource-category",
            element: <PrivateRoute Component={<ResourceCategoryPage />} />,
          },
          {
            path: "/images",
            element: <PrivateRoute Component={<ResourceImagesPage />} />,
          },
          {
            path: "/videos",
            element: <PrivateRoute Component={<ResourceVideosPage />} />,
          },
          {
            path: "/courseware",
            element: <PrivateRoute Component={<ResourceCoursewarePage />} />,
          },
          {
            path: "/course",
            element: <PrivateRoute Component={<CoursePage />} />,
          },
          {
            path: "/courses",
            element: <PrivateRoute Component={<CourseListPage />} />,
          },
          {
            path: "/courses/create",
            element: <PrivateRoute Component={<CourseFormPage />} />,
          },
          {
            path: "/courses/:id",
            element: <PrivateRoute Component={<CourseFormPage />} />,
          },
          {
            path: "/course/user/:courseId",
            element: <PrivateRoute Component={<CourseUserPage />} />,
          },
          {
            path: "/exam/papers",
            element: <PrivateRoute Component={<ExamPaperListPage />} />,
          },
          {
            path: "/exam/papers/create",
            element: <PrivateRoute Component={<ExamPaperFormPage />} />,
          },
          {
            path: "/exam/papers/:id",
            element: <PrivateRoute Component={<ExamPaperFormPage />} />,
          },
          {
            path: "/exam/papers/:id/compose",
            element: <PrivateRoute Component={<ExamPaperComposePage />} />,
          },
          {
            path: "/exam/questions",
            element: <PrivateRoute Component={<QuestionListPage />} />,
          },
          {
            path: "/train/projects",
            element: <PrivateRoute Component={<TrainProjectListPage />} />,
          },
          {
            path: "/train/projects/create",
            element: <PrivateRoute Component={<TrainProjectFormPage />} />,
          },
          {
            path: "/train/projects/:id",
            element: <PrivateRoute Component={<TrainProjectDetailPage />} />,
          },
          {
            path: "/train/projects/:id/tasks",
            element: <PrivateRoute Component={<TrainTaskConfigPage />} />,
          },
          {
            path: "/live/rooms",
            element: <PrivateRoute Component={<LiveRoomListPage />} />,
          },
          {
            path: "/live/rooms/create",
            element: <PrivateRoute Component={<LiveRoomFormPage />} />,
          },
          {
            path: "/point/rules",
            element: <PrivateRoute Component={<PointRuleManagePage />} />,
          },
          {
            path: "/point/products",
            element: <PrivateRoute Component={<PointProductManagePage />} />,
          },
          {
            path: "/point/orders",
            element: <PrivateRoute Component={<PointOrderManagePage />} />,
          },
          {
            path: "/member",
            element: <KeepAlive />,
            children: [
              {
                path: "/member/index",
                element: <PrivateRoute Component={<MemberPage />} />,
              },
              {
                path: "/member/import",
                element: <PrivateRoute Component={<MemberImportPage />} />,
              },
              {
                path: "/member/learn",
                element: <PrivateRoute Component={<MemberLearnPage />} />,
              },
              {
                path: "/member/departmentUser",
                element: (
                  <PrivateRoute Component={<MemberDepartmentProgressPage />} />
                ),
              },
            ],
          },
          {
            path: "/system/config/index",
            element: <PrivateRoute Component={<SystemConfigPage />} />,
          },
          {
            path: "/system/administrator",
            element: <PrivateRoute Component={<SystemAdministratorPage />} />,
          },
          {
            path: "/system/adminroles",
            element: <PrivateRoute Component={<SystemAdminrolesPage />} />,
          },
          {
            path: "/system/adminlog",
            element: <PrivateRoute Component={<SystemLogPage />} />,
          },
          {
            path: "/department",
            element: <PrivateRoute Component={<DepartmentPage />} />,
          },
          {
            path: "/licensing",
            element: <PrivateRoute Component={<LicensingPage />} />,
          },
        ],
      },
      {
        path: "/",
        element: <WithoutHeaderWithoutFooter />,
        children: [
          {
            path: "/login",
            element: <LoginPage />,
          },
          {
            path: "/test",
            element: <TestPage />,
          },
          {
            path: "/error",
            element: <ErrorPage />,
          },
          {
            path: "*",
            element: <ErrorPage />,
          },
        ],
      },
    ],
  },
];

export default routes;
