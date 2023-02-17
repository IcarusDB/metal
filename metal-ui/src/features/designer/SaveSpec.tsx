import { Button } from "@mui/material";
import { useCallback } from "react";
import { VscSave } from "react-icons/vsc";
import { SaveResponse, saveSpecOfId } from "../../api/ProjectApi";
import { State } from "../../api/State";
import { Spec } from "../../model/Spec";
import { useDesignerAsync } from "./DesignerHooks";
import { useMessagsLogger, useMetalFlowFn } from "./DesignerProvider";

function useSaveSpec(token: string | null, id: string): [() => void, State] {
    const {info, error} = useMessagsLogger();
    const [run, status] = useDesignerAsync<SaveResponse>({
        onError: (reason) => {
            error("Fail to Save Spec.");
        },
        onSuccess: () => {
            info("Success to Save Spec.")
        }
    });
    const [getMetalFlowAction] = useMetalFlowFn();

    const save = useCallback(() => {
        if (token === null) {
            return;
        }
        const action = getMetalFlowAction();
        if (action === undefined) {
            return;
        }

        const spec: Spec = action.export();

        run(saveSpecOfId(token, id, spec));
    }, [getMetalFlowAction, id, run, token]);

    return [save, status];
}

export interface SaveSpecProps {
    token: string | null;
    id: string;
}

export function SaveSpec(props: SaveSpecProps) {
    const { token, id } = props;

    const [saveSpec, saveStatus] = useSaveSpec(token, id);
    const onSaveSpec = useCallback(() => {
        saveSpec();
    }, [saveSpec]);
    return (
        <Button
            size="small"
            sx={{
                borderRadius: "0px",
            }}
            variant={"outlined"}
            onClick={onSaveSpec}
            disabled={saveStatus === State.pending}
            startIcon={<VscSave />}
        >
            {saveStatus === State.failure
                ? "FAIL"
                : saveStatus === State.pending
                ? "SAVING..."
                : "SAVE"}
        </Button>
    );
}
