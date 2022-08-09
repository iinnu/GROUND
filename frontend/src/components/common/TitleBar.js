import { AppBar, Toolbar, IconButton, Typography } from "@mui/material";
import { Box } from "@mui/system";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import AutoAwesomeOutlinedIcon from "@mui/icons-material/AutoAwesomeOutlined";

function TitleBar(props) {
  const handleClickTitle = () => {
    document.querySelector(".content").scrollTo(0, 0);
  };

  return (
    <Box sx={{ flexGrow: 1 }} display="flex" justifyContent="center">
      <AppBar id="titlebar" position="static">
        <Toolbar id="titlebar__toolbar">
          {props.isBack ?
            <IconButton
              size="large"
              edge="start"
              color="inherit"
              aria-label="menu"
            >
              <ArrowBackIcon />
            </IconButton>
            : (props.title === "최신 글 피드"
              &&
              <div style={{ width: '50.25px' }}>
              </div>
            )
          }
          <Typography
            className="titlebar__text"
            variant="h6"
            component="div"
            sx={{ flexGrow: 1 }}
          >
            <span onClick={() => handleClickTitle()}>{props.title}</span>
          </Typography>
          {props.title === "최신 글 피드" &&
            <IconButton
              size="large"
              edge="center"
              color="inherit"
              aria-label="filter"
            >
              <AutoAwesomeOutlinedIcon />
            </IconButton>
          }
        </Toolbar>
      </AppBar>
    </Box >
  );
}

export default TitleBar;