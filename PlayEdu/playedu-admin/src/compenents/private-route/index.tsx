import React from "react";
import { getToken } from "../../utils/index";
import { Navigate } from "react-router-dom";

interface PropInterface {
  Component: any;
}

const LOCAL_DEV_BYPASS = import.meta.env.VITE_LOCAL_DEV_BYPASS === "true";

const PrivateRoute: React.FC<PropInterface> = ({ Component }) => {
  return LOCAL_DEV_BYPASS || getToken()
    ? Component
    : <Navigate to="/login" replace={true} />;
};
export default PrivateRoute;
