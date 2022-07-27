import { Grid }  from "@mui/material";
import { Button } from "@mui/material";
import { ThemeProvider } from "@emotion/react";
import theme from "components/common/theme.js";

function IdResult() {
	return (
			<div>
				<Grid item>
					<div>
						등록된 아이디는 userid입니다.
					</div>
				</Grid>
				<Grid item>
					<ThemeProvider theme={theme}>
            <Button
              className="findid-form__button"
              variant="contained"
            >
              로그인 하러 가기
            </Button>
          </ThemeProvider>
				</Grid>
			</div>
	)
};

export default IdResult;